package com.lumos.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// JWT 工具类
@Component
public class JwtUtils {

    @Value("${lumos.jwt.secret}")
    private String secret;

    @Value("${lumos.jwt.expire}")
    private long expire;

    public String generateToken(Long id, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

// 当前登录用户（ThreadLocal）
    public static class UserContext {
        private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

        public static void set(LoginUser user) {
            HOLDER.set(user);
        }

        public static LoginUser get() {
            return HOLDER.get();
        }

        public static Long getId() {
            LoginUser u = HOLDER.get();
            return u == null ? null : u.getId();
        }

        public static String getRole() {
            LoginUser u = HOLDER.get();
            return u == null ? null : u.getRole();
        }

        public static void clear() {
            HOLDER.remove();
        }
    }

// 登录用户信息
    public static class LoginUser {
        private Long id;
        private String username;
        private String role;

        public LoginUser(Long id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
