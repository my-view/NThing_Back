package data.service;

import data.dto.LikeDto;
import data.dto.PurchaseDto;
import data.mapper.LikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LikeService {

    private final LikeMapper likeMapper;

    @Autowired
    public LikeService(LikeMapper likeMapper) {
        this.likeMapper = likeMapper;
    }

    public void createLike(LikeDto likeDto){
        likeMapper.createLike(likeDto);
    }

    public List<PurchaseDto.Summary> findLikedPurchasesByUserId(int userId){
        return likeMapper.findLikedPurchasesByUserId(userId);
    }

    public Optional<Integer> findLikeIdByUserIdAndPurchaseId(int userId, int purchaseId) {
        return likeMapper.findLikeIdByUserIdAndPurchaseId(userId, purchaseId);
    }

    public void deleteLike(int id){
        likeMapper.deleteLike(id);
    }
}
