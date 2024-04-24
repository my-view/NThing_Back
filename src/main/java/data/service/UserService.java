package data.service;

import data.constants.ErrorCode;
import data.dto.*;
import data.exception.UnauthorizedException;
import data.exception.UserNotFoundException;
import data.mapper.CollegeMapper;
import data.mapper.UserMapper;
import data.util.JwtProvider;
import data.util.MultiFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserService {

    private final UserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final JwtProvider jwtProvider;
    private final MultiFileUtils multiFileUtils;

    @Autowired
    public UserService(UserMapper userMapper, CollegeMapper collegeMapper, JwtProvider jwtProvider, MultiFileUtils multiFileUtils) {
        this.userMapper = userMapper;
        this.collegeMapper = collegeMapper;
        this.jwtProvider = jwtProvider;
        this.multiFileUtils = multiFileUtils;
    }

    public UserDto updateUser(Map<String, String> map, MultipartFile file) {
        int userId = jwtProvider.parseJwt(map.get("token"));
        String url = multiFileUtils.getDomain() + multiFileUtils.uploadFile(file, "user").getSave_name();

        UserDto user = new UserDto();
        user.setId(userId);
        user.setNickname(map.get("nickname"));
        user.setEmail(map.get("email"));
        user.setProfileImage(url);
        userMapper.updateUser(user);
        return findById(map.get("token"));
    }

    public List<UserDto> findAll() {
        return userMapper.findAll();
    }

    public UserDto findById(String token) {
        int loginId = jwtProvider.parseJwt(token);
        UserDto user = userMapper.findById(loginId);
        user.setCollege(collegeMapper.selectCollegeById(user.getCollegeId()));
        if (user == null) {
            throw new UserNotFoundException("User not found for the provided token.", ErrorCode.USER_NOT_FOUND);
        }
        return user ;
    }

    public void deleteUser(String token) {
        int loginId = jwtProvider.parseJwt(token);
        userMapper.deleteUser(loginId);
    }

    public ResponseEntity<MessageTokenDto> checkRefreshToken(String token) {
        if (token == null || token.isBlank()){
            throw new UnauthorizedException("토큰 정보 없음", ErrorCode.UNAUTHORIZED);
        } else { // 토큰이 없을 경우
            /*
             * 2-1. refresh token 이 있는 경우 -> 유효성 체크 -> 통과시 엑세스 토큰 리턴 그 후 다시 1-1로
             * 저장된 토큰 정보와 일치해야함
             */
            if (!jwtProvider.isValidRefreshToken(token)) {
                throw new UnauthorizedException("유효하지 않은 토큰", ErrorCode.REFRESH_TOKEN_EXPIRED);
            } else  {
                int id = jwtProvider.parseJwt(token);
                // 저장된 리프레시 토큰과 비교
                String saved_token = userMapper.getTokenById(id);
                if (!saved_token.equals(jwtProvider.BearerRemove(token))) {
                    throw new UnauthorizedException("유효하지 않은 토큰", ErrorCode.UNAUTHORIZED);
                } else {
                    JwtToken jwtToken = JwtToken.builder().token(jwtProvider.createToken(id)).build();

                    return new ResponseEntity<>(new MessageTokenDto("엑세스 토큰 재발급 요청 성공", jwtToken.getToken(), null), HttpStatus.OK);
                }
            }

        }
    }
}
