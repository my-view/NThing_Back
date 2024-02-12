package data.mapper;

import data.dto.PurchaseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface PurchaseMapper {
    int createPurchase(PurchaseDto.Request purchase);
    void setLocation(Map<String, Object> parameters);
    List<PurchaseDto.Summary> findAllPurchase(Map<String, Object> map);
    PurchaseDto.Detail findPurchaseById(Map<String, Object> map);
    boolean findPurchaseByIdAndUserId(Map<String, Object> map);
    int updatePurchase(PurchaseDto.Request purchase);
    void deletePurchase(int id);
}
