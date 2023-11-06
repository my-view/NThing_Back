package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

public class CommentDto {

    @Getter
    @Builder
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Comment {
        private int id;
        private String content;
        private boolean isPrivate;
        private boolean isDelete;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp updatedAt;
        private int userId;
        private String nickname;
        private int purchaseId;
        private int parentId;

        public boolean getIsPrivate() {
            return isPrivate;
        }

        public void setIsPrivate(boolean isPrivate) {
            this.isPrivate = isPrivate;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Request {
        private int id;
        private String content;
        private boolean isPrivate;
        private int userId;
        private int purchaseId;
        private int parentId;

        public boolean getIsPrivate() {
            return isPrivate;
        }
    }

    @Getter
    @Builder
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Response {
        private int id;
        private String content;
        private String nickname;
        private String profileImage;
        private boolean isPrivate;
        @JsonProperty("is_authorized")
        private boolean isAuthorized;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp updatedAt;
        @JsonIgnore
        private int userId;
        @JsonIgnore
        private int parentId;
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<CommentDto.Response> replies;

        public boolean getIsPrivate() {
            return isPrivate;
        }

        @JsonIgnore
        public boolean getIsAuthorized() {
            return isAuthorized;
        }

        public void setIsAuthorized(boolean authorized) {
            isAuthorized = authorized;
        }
    }
}