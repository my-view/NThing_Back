package data.dto;

import lombok.*;
import org.apache.ibatis.type.Alias;


@Getter
@Setter
@Builder
@AllArgsConstructor
@Alias("PurchaseUserDto")
public class PurchaseUserDto {
    private int userId;
    private int purchaseId;
}
