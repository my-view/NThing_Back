package data.util;

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
    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createToken(int loginId) {
        Claims claims = Jwts.claims();
        claims.put("loginId",loginId);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofDays(1).toMillis()); // 만료기간 1일

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .setIssuer("test") // 토큰발급자(iss)
                .setIssuedAt(now) // 발급시간(iat)
                .setExpiration(expiration) // 만료시간(exp)
                .setClaims(claims)
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

    private String BearerRemove(String token) {
        return token.substring("Bearer ".length());
    }
}