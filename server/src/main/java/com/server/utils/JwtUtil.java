package com.server.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.server.user.entity.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 生成token
     */
    public String generateToken(UserInfo user) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("alg", "HS256");
        headerClaims.put("typ", "JWT");

        return JWT.create()
                .withHeader(headerClaims)
                .withClaim("userId", user.getId())
                .withClaim("username", user.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证token
     */
    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            return verifier.verify(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("userId").asLong();
        }
        return null;
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getClaim("username").asString();
        }
        return null;
    }

    /**
     * 检查token是否过期
     */
    public boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        if (decodedJWT != null) {
            return decodedJWT.getExpiresAt().before(new Date());
        }
        return true;
    }

    /**
     * 获取过期时间
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * 获取header
     */
    public String getHeader() {
        return header;
    }

    /**
     * 获取prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
