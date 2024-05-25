package data.controller;

import data.dto.ApiResult;
import data.dto.ErrorResponse;
import data.dto.MessageTokenDto;
import data.service.UserService;
import data.util.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "test", description = "테스트용 API")
@RestController
@RequiredArgsConstructor
public class TestController {
    private final JwtProvider jwtProvider;
    private final UserService userService;

    @Operation(summary = "테스트용 토큰 발급", description = "path variable로 id를 받아서 토큰을 발급합니다.")
    @Parameter(name = "id", description = "테스트용 토큰 발급을 위한 user_id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "테스트용 토큰 발급 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResult.class))),
            @ApiResponse(responseCode = "400", description = "테스트용 토큰 발급 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/test/token/{id}")
    public ResponseEntity<ApiResult<MessageTokenDto>> getTestToken(@PathVariable int id) {
        String accessToken = jwtProvider.createToken(id);
        String refreshToken = jwtProvider.createRefreshToken(id);
        int userId = jwtProvider.parseJwt(accessToken);
        userService.findById(userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResult.created(new MessageTokenDto("테스트 토큰 생성", accessToken, refreshToken)));
    }
}
