package data.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import data.constants.ErrorCode;
import data.dto.ChatRoomDto;
import data.dto.FileDto;
import data.dto.PurchaseDto;
import data.dto.PurchaseUserDto;
import data.exception.AllParticipantsJoinedException;
import data.exception.AlreadyJoinedException;
import data.exception.InvalidRequestException;
import data.exception.PurchaseNotFoundException;
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


    public PurchaseDto.Detail createPurchase(PurchaseDto.Request purchaseRequest, String token) {
        if (isValidDate(purchaseRequest.getDate()))
            throw new InvalidRequestException("Invalid date: " + purchaseRequest.getDate(), ErrorCode.INVALID_INPUT_VALUE);
        int userId = jwtProvider.parseJwt(token);
        purchaseRequest.setManager_id(userId);
        purchaseMapper.createPurchase(purchaseRequest);
        int createdPurchaseID = purchaseRequest.getId();
        int purchaseId = purchaseRequest.getId();
        List<FileDto.Request> files = multiFileUtils.uploadFiles(purchaseRequest.getAdded_files(), "purchase");
        fileService.saveFiles(purchaseId, files);

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

    public PurchaseDto.Detail findPurchaseById(int purchaseId, String token) {
        Map<String, Object> map = Map.of("id", purchaseId, "user_id", jwtProvider.parseJwt(token));
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
        return purchase;
    }

    public PurchaseDto.Detail updatePurchase(PurchaseDto.Request purchaseRequest, String token, int id) {
        int userId = jwtProvider.parseJwt(token);
        Map<String, Object> map = Map.of("id", id, "user_id", userId);
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
        Map<String, Object> map = Map.of("id", id, "user_id", userId);
        if (purchaseMapper.findPurchaseByIdAndUserId(map)) {
            purchaseMapper.deletePurchase(id);
            fileService.deleteAllFileByIds(fileService.findAllIdsByPurchaseId(id));
        } else {
            throw new PurchaseNotFoundException("Purchase not found for id: " + id, ErrorCode.PURCHASE_NOT_FOUND);
        }
    }

    public void joinPurchase(int purchaseId, String token) {
        int userId = jwtProvider.parseJwt(token);
        Map<String, Integer> data = Map.of(
                "purchaseId", purchaseId,
                "userId", userId
        );

        Map<String, Object> map = Map.of("id", purchaseId, "user_id", userId);
        PurchaseDto.Detail purchase = purchaseMapper.findPurchaseById(map);

        if (purchase == null) {
            throw new PurchaseNotFoundException("Purchase not found for ID: " + purchaseId, ErrorCode.PURCHASE_NOT_FOUND);
        }

        if (purchase.getDenominator() == purchase.getNumerator()) {
            throw new AllParticipantsJoinedException("All participants have already joined", ErrorCode.ALL_PARTICIPANTS_JOINED);
        }

        PurchaseUserDto purchaseUserDto = purchaseUserMapper.findByPurchaseIdAndUserId(data);
        if (purchaseUserDto != null) {
            throw new AlreadyJoinedException("already joined", ErrorCode.ALREADY_JOINED);
        }

        purchaseUserMapper.createPurchaseUser(data);
        Map<String, Integer> param = Map.of(
                "id", purchaseId,
                "numerator", purchase.getNumerator() + 1
        );
        purchaseMapper.joinPurchase(param);
    }

    public List<PurchaseDto.Summary> getUserPurchase(Map<String, Object> map) {
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
}
