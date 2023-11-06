package data.mapper;

import data.dto.LikeDto;
import data.dto.PurchaseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LikeMapper {
    int createLike(LikeDto likeDto);
    List<PurchaseDto.Summary> findLikedPurchasesByUserId(int userId);
    Optional<Integer> findLikeIdByUserIdAndPurchaseId(@Param("user_id") int userId, @Param("purchase_id") int purchaseId);
    void deleteLike(int id);
}
