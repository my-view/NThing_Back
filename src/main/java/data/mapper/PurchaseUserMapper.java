package data.mapper;

import data.dto.PurchaseUserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface PurchaseUserMapper {
    void createPurchaseUser(Map<String, Integer> data);
    PurchaseUserDto findByPurchaseIdAndUserId(Map<String, Integer> data);
}
