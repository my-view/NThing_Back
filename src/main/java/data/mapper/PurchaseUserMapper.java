package data.mapper;

import data.dto.PurchaseUserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface PurchaseUserMapper {
    void createPurchaseUser(PurchaseUserDto purchaseUserDto);
    void deletePurchaseUser(PurchaseUserDto purchaseUserDto);
    PurchaseUserDto findByPurchaseIdAndUserId(PurchaseUserDto purchaseUserDto);
}
