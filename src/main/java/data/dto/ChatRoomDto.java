package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class ChatRoomDto {
    private int id;
    private int purchaseId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;
    private Boolean isDeleted;
    private Boolean isCompleted;

    @Builder
    public ChatRoomDto(int id, int purchaseId) {
        this.id = id;
        this.purchaseId = purchaseId;
    }
}
