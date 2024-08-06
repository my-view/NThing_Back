package data.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import data.constants.ErrorCode;
import data.dto.*;
import data.mapper.UserMapper;
import data.util.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginService {

    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;

    @Autowired
    public LoginService(UserMapper userMapper, JwtProvider jwtProvider) {
        this.userMapper = userMapper;
        this.jwtProvider = jwtProvider;
    }


    public MessageTokenDto googleLogin(IdToken token) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token.getIdToken());
        UserDto.User user = userMapper.findUser(UserDto.Search.builder()
                .email(decodedToken.getEmail())
                .build());
        if (user != null) {
            return handleLoginSuccess(decodedToken);
        } else {
            return handleSignupSuccess(decodedToken);
        }
    }

    private MessageTokenDto handleLoginSuccess(FirebaseToken decodedToken) {
        UserDto.User user = userMapper.findUser(UserDto.Search.builder()
                .email(decodedToken.getEmail())
                .build());

        JwtToken accessToken = JwtToken.builder().token(jwtProvider.createToken(user.getId())).build();
        JwtToken refreshToken = JwtToken.builder().token(jwtProvider.createRefreshToken(user.getId())).build();

        UserDto.Update userUpdate = UserDto.Update.builder()
                .refreshToken(refreshToken.getToken())
                .build();
        userUpdate.setId(userUpdate.getId());
        userMapper.updateUser(userUpdate);

        return new MessageTokenDto("구글 로그인 성공", accessToken.getToken(), refreshToken.getToken());
    }

    private MessageTokenDto handleSignupSuccess(FirebaseToken decodedToken) {
        JwtToken refreshToken = JwtToken.builder().token(signUp(decodedToken)).build();
        JwtToken jwtToken = JwtToken.builder().token(signUp(decodedToken)).build();
        return new MessageTokenDto("구글 로그인 성공", jwtToken.getToken(), refreshToken.getToken());
    }

    public String signUp(FirebaseToken decodedToken) {
        String identity = decodedToken.getClaims().get("firebase").toString();
        String provider = identity.split("sign_in_provider=")[1].split("\\.")[0];
        String providerId = identity.split("google.com=\\[")[1].split("]")[0];

        UserDto.Create user = UserDto.Create.builder()
                .nickname(decodedToken.getName())
                .email(decodedToken.getEmail())
                .profileImage(decodedToken.getPicture())
                .providerId(providerId)
                .provider(provider)
                .build();

        return jwtProvider.createToken(userMapper.join(user));
    }

    public ResponseEntity<ApiResult<?>> kakaoLogin(IdToken token) {
        // 카카오에 사용자 정보 요청
        String kakaoAccesstoken = token.getIdToken();
        JsonNode userInfo = getUserInfo(kakaoAccesstoken);
        UserDto.User user = userMapper.findUser(UserDto.Search.builder()
                .email(userInfo.get("kakao_account").get("email").asText())
                .build());

        if(!userInfo.get("kakao_account").get("has_email").asText().equals("true")) {
            // 사용자의 이메일 정보가 없는 경우 -> 오류
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_INPUT_VALUE);
            errorResponse.setMessage("이메일 정보 없음");
            return ResponseEntity.badRequest().body(ApiResult.error(errorResponse));
        }
        // 가입된 사용자가 아닌 경우
        if(user == null) {
            // 현재 가입되지 않은 사용자의 경우 -> 회원가입
            String profileImage = userInfo.at("/properties/profile_image").asText();
            profileImage = profileImage.replaceFirst("http://", "https://");
            UserDto.Create userCreate = UserDto.Create.builder()
                    .provider("kakao")
                    .providerId(userInfo.at("/id").asText())
                    .nickname(userInfo.at("/kakao_account/profile/nickname").asText())
                    .email(userInfo.at("/kakao_account/email").asText())
                    .profileImage(profileImage)
                    .build();
            userMapper.join(userCreate);
        }

        //로그인
        int findId = userMapper.findUser(UserDto.Search.builder()
                .email(userInfo.get("kakao_account").get("email").asText())
                .build()).getId();

        JwtToken refreshToken = JwtToken.builder().token(jwtProvider.createRefreshToken(findId)).build();

        UserDto.Update userUpdate = UserDto.Update.builder()
                .refreshToken(refreshToken.getToken())
                .build();
        userUpdate.setId(findId);
        userMapper.updateUser(userUpdate);

        JwtToken jwtToken = JwtToken.builder().token(jwtProvider.createToken(findId)).build();
        MessageTokenDto messageTokenDto = new MessageTokenDto("카카오 로그인 성공", jwtToken.getToken(), refreshToken.getToken());

        return ResponseEntity.ok(ApiResult.ok(messageTokenDto));
    }

    // 유저정보 요청(카카오)
    private JsonNode getUserInfo(String kakaoAccessToken) {
        // properties에 등록
        String resourceUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);

        ResponseEntity<JsonNode> response = new RestTemplate().exchange(
                resourceUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );

        HttpStatus statusCode = response.getStatusCode();
        if (statusCode != HttpStatus.OK) {
            throw new RuntimeException("Kakao API 호출 중 오류 발생: " + statusCode);
        }

        System.out.println(response.getBody());
        return response.getBody();
    }

}