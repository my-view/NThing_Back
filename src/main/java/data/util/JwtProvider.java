package data.util;

import data.constants.ErrorCode;
import data.exception.JsonProcessingException;
import data.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final String secret;

    @Autowired
    public JwtProvider(@Value("${jwt.secret:defaultSecretValue}") String secret) {
        this.secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createToken(int loginId) {
        Claims claims = Jwts.claims();
        claims.put("loginId",loginId);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofMinutes(1).toMillis()); // 만료기간 1분
//        Date expiration = new Date(now.getTime() + Duration.ofDays(1).toMillis()); // 만료기간 1일

        return Jwts.builder()
                .setClaims(claims)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .setIssuer("test") // 토큰발급자(iss)
                .setIssuedAt(now) // 발급시간(iat)
                .setExpiration(expiration) // 만료시간(exp)
                .signWith(SignatureAlgorithm.HS256, secret) // 알고리즘, 시크릿 키
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(int loginId) {
        Claims claims = Jwts.claims();
        claims.put("loginId",loginId);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofMinutes(2).toMillis()); // 만료기간 2분
//        Date expiration = new Date(now.getTime() + Duration.ofDays(30).toMillis()); // 만료기간 30일
        claims.put("expiration", expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .setIssuer("test") // 토큰발급자(iss)
                .setIssuedAt(now) // 발급시간(iat)
                .setExpiration(expiration) // 만료시간(exp)
                .signWith(SignatureAlgorithm.HS256, secret) // 알고리즘, 시크릿 키
                .compact();
    }

    public int parseJwt(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(BearerRemove(token))
                .getBody()
                .get("loginId", Integer.class);
    }

    // 액세스 토큰 유효성 확인
    public boolean isValidAccessToken(String token) {
        try {
            return isValidToken(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("invalid access token", ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtException ex) {
            throw new JsonProcessingException("invalid input value", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 리프레시 토큰 유효성 확인
    public boolean isValidRefreshToken(String token) {
        try {
            return isValidToken(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("invalid refresh token", ErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (JwtException ex) {
            throw new JsonProcessingException("invalid input value", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 토큰 유효성 확인
    public boolean isValidToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(BearerRemove(token))
                .getBody();
        return claims.getExpiration().after(new Date());

    }

    public String BearerRemove(String token) {
        String prefix = "Bearer ";

        if (token.startsWith(prefix)) {
            return token.substring(prefix.length());
        } else {
            return token;
        }
    }
}
