package data.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.constants.ErrorCode;
import data.dto.ErrorResponse;
import data.exception.AccessTokenExpiredException;
import data.exception.UnauthorizedException;
import data.util.JwtProvider;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.MediaType;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFilter implements Filter {

    private JwtProvider jwtProvider;
    public LoginFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    // 로그인 검증에서 제외할 URI들
    private static final String[] whitelist = {
            "/",
            "/login/*",
            "/favicon.ico",
            "/test/*",
            "/comment/purchase/*",
            "/purchases",
            "/categories",
            "/college",
            "/college/*",
            "/file/*",
            "/category",
            /* swagger 관련 URI */
            "/v3/api-docs/*",
            "/v3/api-docs",
            "/swagger-ui/*",
            "/docs",
            "/api-docs",
            "/api-docs/*",
            /* 채팅 서비스 테스트용 */
            "/ws-stomp",
            "/user/retoken"
    }; // 추가 필요

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        /*
        * 검증 절차 (header)
        * 1-1. access token 이 있는 경우 -> 유효성 체크 -> 통과시 실행, 통과하지 못할시 1-2로
        * 1-2. access token 이 없는 경우 -> 엑세스 토큰이 없다는 애러 리턴
        */

        String accessToken = httpRequest.getHeader("Authorization");

        try {
            if (isLoginCheckPath(requestURI)) { // 검증해야하는 URI인 경우
                if (accessToken == null) { // 엑세스 토큰이 null이 아니면
                    throw new UnauthorizedException("null token", ErrorCode.UNAUTHORIZED);
                }
                if (!jwtProvider.isValidAccessToken(accessToken)) { // 유효하지 않은 토큰의 경우
                    throw new AccessTokenExpiredException("invalid token", ErrorCode.ACCESS_TOKEN_EXPIRED);
                }
            }
            chain.doFilter(request, response);
        } catch (UnauthorizedException e) {
            setErrorResponse(httpResponse, ErrorCode.UNAUTHORIZED);
        } catch (ExpiredJwtException | AccessTokenExpiredException e) {
            setErrorResponse(httpResponse, ErrorCode.ACCESS_TOKEN_EXPIRED);
        }
    }

    private void setErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(errorCode);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 화이트리스트는 인증 체크를 하지 않음 */
    private boolean isLoginCheckPath(String requestURI) {
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
    }
}
