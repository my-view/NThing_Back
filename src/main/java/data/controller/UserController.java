package data.controller;

import data.constants.ErrorCode;
import data.dto.*;
import data.service.UserService;
import data.service.LikeService;
import data.util.JwtProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LikeService likeService;
    private final JwtProvider jwtProvider;

    @GetMapping("")
    public ResponseEntity<ApiResult<?>> findById(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(ApiResult.ok(userService.findById(jwtProvider.parseJwt(token))));
    }

    @PatchMapping("")
    public ResponseEntity<ApiResult<?>> updateUser(
            @RequestHeader("Authorization") String token,
            @ModelAttribute UserDto.Update user
    ) {
        if(isAllFieldsNull(user)) {
            return ResponseEntity.badRequest().body(ApiResult.error(new ErrorResponse(ErrorCode.BAD_REQUEST)));
        }
        user.setId(jwtProvider.parseJwt(token));
        return ResponseEntity.ok(ApiResult.ok(userService.updateUser(user)));
    }

    @GetMapping("/like")
    public ResponseEntity<ApiResult<List<PurchaseDto.Summary>>> findLikedPurchasesByUserId(
            @RequestHeader("Authorization") String token,
            String search_keyword,
            String sort
    ) {
        int userId = jwtProvider.parseJwt(token);
        Map<String, Object> map = new HashMap<>();
        map.put("search_keyword", search_keyword);
        map.put("sort", sort);
        map.put("user_id", userId);

        return ResponseEntity.ok(ApiResult.ok(likeService.findLikedPurchasesByUserId(map)));
    }

    @DeleteMapping("")
    public ResponseEntity<ApiResult<?>> deleteUser(@RequestHeader("Authorization") String token) {
        userService.deleteUser(jwtProvider.parseJwt(token));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    // 리프레시 토큰 검증
    @PostMapping("/retoken")
    public ResponseEntity<MessageTokenDto> checkRefreshToken(@RequestHeader("Authorization") String token) {
        return userService.checkRefreshToken(token);
    }

    private boolean isAllFieldsNull(UserDto.Update dto) {
        return dto.getNickname() == null &&
                dto.getProfileImage() == null &&
                dto.getCollegeId() == null;
    }
}
