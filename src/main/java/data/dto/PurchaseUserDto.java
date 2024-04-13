package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.sql.Timestamp;

@Data
@Alias("PurchaseUserDto")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PurchaseUserDto {
    private int id;
    private int userId;
    private int purchaseId;
    private int share;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;
}
