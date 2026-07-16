package com.example.project.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";
    private static final String CLAIM_ROLE = "role";

    private static final String SECRET =
            "TeachingFeedback-JWT-Secret-Key-At-Least-32-Bytes!!";
    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION = 1000L * 60 * 60;

    /** 生成 JWT（subject 为用户登录名，role 为角色） */
    public static String generateToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    /** 解析并校验签名、过期时间，返回 Claims */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 token 中取 subject（登录名） */
    public static String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    /** 从 token 中取角色 */
    public static String getRole(String token) {
        return parseToken(token).get(CLAIM_ROLE, String.class);
    }
}
