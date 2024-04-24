package data.service;

import data.dto.LikeDto;
import data.dto.PurchaseDto;
import data.mapper.LikeMapper;
import data.util.MultiFileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeMapper likeMapper;
    private final MultiFileUtils multiFileUtils;

    public void createLike(LikeDto likeDto){
        likeMapper.createLike(likeDto);
    }

    public List<PurchaseDto.Summary> findLikedPurchasesByUserId(Map<String, Object> map){
        List<PurchaseDto.Summary> result = likeMapper.findLikedPurchasesByUserId(map);
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

    public Optional<Integer> findLikeIdByUserIdAndPurchaseId(int userId, int purchaseId) {
        return likeMapper.findLikeIdByUserIdAndPurchaseId(userId, purchaseId);
    }

    public void deleteLike(int id){
        likeMapper.deleteLike(id);
    }
}
