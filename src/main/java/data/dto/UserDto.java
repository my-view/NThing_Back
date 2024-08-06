package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.beans.ConstructorProperties;
import java.sql.Timestamp;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.springframework.web.multipart.MultipartFile;

public class UserDto {

    @Getter
    public static class User {
        private int id;
        private String provider;
        private String providerId;
        private String nickname;
        private String email;
        private String profileImage;
        private int credit;
        private Timestamp subscriptionDate;
        private int collegeId;
        private String refreshToken;
    }

    @Getter
    @Setter
    @Builder
    public static class Create {
        private String provider;
        private String providerId;
        private String nickname;
        private String email;
        private String profileImage;
    }

    @Getter
    @Setter
    public static class Update {
        private int id;
        private String nickname;
        private MultipartFile profileImage;
        private String profileImageUrl;
        private Integer collegeId;
        private String refreshToken;

        @Builder
        @ConstructorProperties({"nickname", "profile_image", "college_id", "refresh_token"})
        public Update(String nickname, MultipartFile profileImage, Integer collegeId, String refreshToken) {
            this.nickname = nickname;
            this.profileImage = profileImage;
            this.collegeId = collegeId;
            this.refreshToken = refreshToken;
        }
    }

    @Getter
    @Setter
    @Builder
    public static class Search {
        private int id;
        private String email;
    }

    @Getter
    @Builder
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Detail {
        private int id;
        private String provider;
        private String nickname;
        private String email;
        private String profileImage;
        private int credit;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp subscriptionDate;
        private CollegeDto college;
    }
}
