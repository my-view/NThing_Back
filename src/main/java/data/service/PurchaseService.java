package data.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import data.constants.ErrorCode;
import data.dto.*;
import data.exception.*;
import data.mapper.FileMapper;
import data.mapper.PurchaseMapper;
import data.mapper.PurchaseUserMapper;
import data.util.JwtProvider;
import data.util.MultiFileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseMapper purchaseMapper;
    private final FileService fileService;
    private final FileMapper fileMapper;
    private final MultiFileUtils multiFileUtils;
    private final JwtProvider jwtProvider;
    private final HttpServletRequest request;
    private final ChatService chatService;
    private final PurchaseUserMapper purchaseUserMapper;
    private final UserService userService;

    public PurchaseDto.Detail createPurchase(PurchaseDto.Request purchaseRequest, String token) {
        if (isValidDate(purchaseRequest.getDate()))
            throw new InvalidRequestException("Invalid date: " + purchaseRequest.getDate(), ErrorCode.INVALID_INPUT_VALUE);
        int userId = jwtProvider.parseJwt(token);

        // manager_id 추가
        purchaseRequest.setManager_id(userId);

        // 거래 생성
        purchaseMapper.createPurchase(purchaseRequest);

        int createdPurchaseID = purchaseRequest.getId();
        int purchaseId = purchaseRequest.getId();

        // 거래의 이미지 업로드
        List<FileDto.Request> files = multiFileUtils.uploadFiles(purchaseRequest.getAdded_files(), "purchase");
        fileService.saveFiles(purchaseId, files);

        // purchaseUser 생성
        PurchaseUserDto purchaseUserDto = PurchaseUserDto.builder()
                .purchaseId(purchaseId)
                .userId(userId)
                .build();
        purchaseUserMapper.createPurchaseUser(purchaseUserDto);

        // 채팅방 생성
        chatService.createChatRoom(ChatRoomDto.builder().purchaseId(purchaseId).build());
        return findPurchaseById(createdPurchaseID, token);
    }

    public List<PurchaseDto.Summary> findAllPurchase(Map<String, Object> map) {
        int zoom = (int) map.get("zoom");
        map.put("radius", getRadius(zoom));
        // 첫 번째 쿼리: 위치 설정
        purchaseMapper.setLocation(Map.of("latitude", map.get("latitude"), "longitude", map.get("longitude")));
        // 두 번째 쿼리: 데이터 조회
        String token = (String) map.get("token");
        if (StringUtils.hasText(token)) {
            int userId = jwtProvider.parseJwt(token);
            map.put("userId", userId);
        }
        List<PurchaseDto.Summary> result = purchaseMapper.findAllPurchase(map);
        List<PurchaseDto.Summary> generatePurchaseDtoList = new ArrayList<>();
        for (PurchaseDto.Summary purchaseDto : result) {
            String image = purchaseDto.getImage();
            purchaseDto.setImage(image != null
                            ? multiFileUtils.getDomain() + image
                            : null
            );
            generatePurchaseDtoList.add(purchaseDto);
        }
        return generatePurchaseDtoList;
    }

    public List<PurchaseDto.Summary> findByUserId(Map<String, Object> map) {
        String token = (String) map.get("token");
        if (StringUtils.hasText(token)) {
            int userId = jwtProvider.parseJwt(token);
            map.put("userId", userId);
        }
        List<PurchaseDto.Summary> result = purchaseMapper.findByUserId(map);
        List<PurchaseDto.Summary> generatePurchaseDtoList = new ArrayList<>();
        for (PurchaseDto.Summary purchaseDto : result) {
            String image = purchaseDto.getImage();
            purchaseDto.setImage(image != null
                    ? multiFileUtils.getDomain() + image
                    : null
            );
            generatePurchaseDtoList.add(purchaseDto);
        }
        return generatePurchaseDtoList;
    }

    public PurchaseDto.Detail findPurchaseById(int purchaseId, String token) {
        Map<String, Object> map = Map.of("id", purchaseId, "userId", jwtProvider.parseJwt(token));
        PurchaseDto.Detail purchase = purchaseMapper.findPurchaseById(map);
        if (purchase == null)
            throw new PurchaseNotFoundException("Purchase not found for ID: " + purchaseId, ErrorCode.PURCHASE_NOT_FOUND);

        List<FileDto.Response> fileList = fileMapper.findAllByPurchaseId(purchaseId);
        List<PurchaseDto.Detail.ImageDto> imageList = new ArrayList<>();
        if (!fileList.isEmpty()) {
            List<FileDto.Response> generatedFiles = multiFileUtils.generateFilePath(fileList);
            for (FileDto.Response file : generatedFiles) {
                PurchaseDto.Detail.ImageDto imageDto = PurchaseDto.Detail.ImageDto.builder()
                        .id(file.getId())
                        .url(file.getSaveName())
                        .build();
                imageList.add(imageDto);
            }
        }
        purchase.setImages(imageList);

        UserDto.Detail manager = userService.findById(purchase.getManagerId());
        purchase.setManager(manager);

        return purchase;
    }

    public PurchaseDto.Detail updatePurchase(PurchaseDto.Request purchaseRequest, String token, int id) {
        int userId = jwtProvider.parseJwt(token);
        Map<String, Object> map = Map.of("id", id, "userId", userId);
        if (purchaseMapper.findPurchaseByIdAndUserId(map)) {
            purchaseMapper.updatePurchase(purchaseRequest);
            int purchaseId = purchaseRequest.getId();
            List<FileDto.Request> uploadFiles = multiFileUtils.uploadFiles(purchaseRequest.getAdded_files(), "purchase");
            fileService.saveFiles(purchaseRequest.getId(), uploadFiles);
            fileService.deleteAllFileByIds(purchaseRequest.getRemoved_files());
            return findPurchaseById(purchaseId, token);
        } else {
            throw new PurchaseNotFoundException("Purchase not found for id: " + id, ErrorCode.PURCHASE_NOT_FOUND);
        }
    }

    public void deletePurchase(int id, String token) {
        int userId = jwtProvider.parseJwt(token);
        Map<String, Object> map = Map.of("id", id, "userId", userId);
        if (purchaseMapper.findPurchaseByIdAndUserId(map)) {
            purchaseMapper.deletePurchase(id);
            fileService.deleteAllFileByIds(fileService.findAllIdsByPurchaseId(id));
        } else {
            throw new PurchaseNotFoundException("Purchase not found for id: " + id, ErrorCode.PURCHASE_NOT_FOUND);
        }
    }

    public void joinPurchase(PurchaseUserDto purchaseUserDto) {
        PurchaseDto.Detail purchase = validateAndGetPurchase(purchaseUserDto);

        if (purchase.getDenominator() == purchase.getNumerator()) {
            throw new AllParticipantsJoinedException("All participants have already joined", ErrorCode.ALL_PARTICIPANTS_JOINED);
        }

        if (purchaseUserMapper.findByPurchaseIdAndUserId(purchaseUserDto) != null) {
            throw new AlreadyJoinedException("already joined", ErrorCode.ALREADY_JOINED);
        }

        purchaseUserMapper.createPurchaseUser(purchaseUserDto);
        updatePurchaseNumerator(purchase, 1);
    }

    public void leavePurchase(PurchaseUserDto purchaseUserDto) {
        PurchaseDto.Detail purchase = validateAndGetPurchase(purchaseUserDto);

        if (purchaseUserMapper.findByPurchaseIdAndUserId(purchaseUserDto) == null) {
            throw new NotJoinedException("Not joined in this trade", ErrorCode.NOT_JOINED);
        }

        purchaseUserMapper.deletePurchaseUser(purchaseUserDto);
        updatePurchaseNumerator(purchase, -1);
    }

    public List<PurchaseDto.Summary> findByManagerId(Map<String, Object> map) {
        String token = (String) map.get("token");
        if (StringUtils.hasText(token)) {
            int userId = jwtProvider.parseJwt(token);
            map.put("userId", userId);
        }
        List<PurchaseDto.Summary> result = purchaseMapper.findByManagerId(map);
        List<PurchaseDto.Summary> generatePurchaseDtoList = new ArrayList<>();
        for (PurchaseDto.Summary purchaseDto : result) {
            String image = purchaseDto.getImage();
            purchaseDto.setImage(image != null
                    ? multiFileUtils.getDomain() + image
                    : null
            );
            generatePurchaseDtoList.add(purchaseDto);
        }
        return generatePurchaseDtoList;
    }

    private boolean isValidDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(dateString, formatter);
        LocalDateTime currentTime = LocalDateTime.now();
        return date.isBefore(currentTime);
    }

    private int getRadius(int zoom) {
        int radius = 350;
        switch(zoom) {
            case 15: radius=300;
                break;
            case 16: radius=250;
                break;
            case 17: radius=200;
                break;
            case 18: radius=150;
                break;
            case 19: radius=100;
                break;
        }
        return radius;
    }

    private PurchaseDto.Detail validateAndGetPurchase(PurchaseUserDto purchaseUserDto) {
        Map<String, Object> map = Map.of(
                "id", purchaseUserDto.getPurchaseId(),
                "user_id", purchaseUserDto.getUserId()
        );
        PurchaseDto.Detail purchase = purchaseMapper.findPurchaseById(map);

        if (purchase == null) {
            throw new PurchaseNotFoundException("Purchase not found for ID: " + purchaseUserDto.getPurchaseId(), ErrorCode.PURCHASE_NOT_FOUND);
        }
        return purchase;
    }

    private void updatePurchaseNumerator(PurchaseDto.Detail purchase, int change) {
        PurchaseDto.Request purchaseRequest = PurchaseDto.Request.builder()
                .id(purchase.getId())
                .title(purchase.getTitle())
                .description(purchase.getDescription())
                .latitude(purchase.getLatitude())
                .longitude(purchase.getLongitude())
                .date(String.valueOf(purchase.getDate()))
                .denominator(purchase.getDenominator())
                .numerator(purchase.getNumerator() + change)
                .price(purchase.getPrice())
                .place(purchase.getPlace())
                .category_id(purchase.getCategoryId())
                .build();

        purchaseMapper.updatePurchase(purchaseRequest);
    }
}
