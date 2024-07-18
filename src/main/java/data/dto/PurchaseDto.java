package data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private int id;

        @NotBlank(message="제목은 필수 입니다.")
        private String title;

        @NotBlank(message="설명은 필수 입니다.")
        private String description;

        @NotNull(message="위도는 필수 입니다.")
        private Double latitude;

        @NotNull(message="경도는 필수 입니다.")
        private Double longitude;

        @NotNull(message = "거래 예정일은 필수 입니다.")
        @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$", message = "올바른 날짜 형식이 아닙니다. (yyyy-MM-dd HH:mm:ss)")
        private String date;

        @NotNull(message="총 인원은 필수 입니다.")
        @Min(value = 2, message = "총 인원은 2명 이상이어야 합니다.")
        private int denominator;

        private int numerator = 1;

        private boolean is_completed = false;

        @NotNull(message="가격은 필수 입니다.")
        private int price;

        @NotBlank(message="거래 장소는 필수 입니다.")
        private String place;

        private List<MultipartFile> added_files = new ArrayList<>();

        private int manager_id;

        @NotNull(message="카테고리는 필수입니다.")
        private Integer category_id;

        // 삭제할 첨부파일 id List
        private List<Integer> removed_files;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Summary {
        private int id;
        private String title;
        private Double latitude;
        private Double longitude;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp date;
        private int denominator;
        private int numerator;
        private int price;
        private String place;
        private int isCompleted;
        private boolean isLiked;
        private String image;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Detail {
        private int id;
        private String title;
        private String description;
        private Double latitude;
        private Double longitude;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp date;
        private int denominator;
        private int numerator;
        private int price;
        private String place;
        private int categoryId;
        private String categoryName;
        private int isLiked;
        private int isCompleted;
        private int isJoined;
        private int isManager;
        @JsonIgnore
        private int managerId;
        @JsonIgnoreProperties({"provider", "provider_id", "email", "credit", "subscription_date", "college"})
        private UserDto manager;
        private List<ImageDto> images;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private Timestamp updatedAt;

        @Getter
        @Setter
        @Builder
        public static class ImageDto {
            private int id;
            private String url;
        }
    }
}
