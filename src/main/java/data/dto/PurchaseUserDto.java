package data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.apache.ibatis.type.Alias;

@Data
@Alias("PurchaseUserDto")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PurchaseUserDto {
    private int id;
    private int userId;
    private int purchaseId;
}
