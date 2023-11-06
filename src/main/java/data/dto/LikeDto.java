package data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LikeDto {
    private int id;
    private int userId;
    private int purchaseId;

    public static class Request {
        private boolean value;

        public boolean isValue() {
            return value;
        }
    }
}
