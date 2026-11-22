package com.lumos.common;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 登录校验拦截器：所有 /api 请求除白名单外必须携带有效 Token
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public AuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String uri = request.getRequestURI();
        // 白名单：登录、注册、公开浏览类接口
        if (isPublic(uri)) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\"}");
            return false;
        }
        try {
            Claims claims = jwtUtils.parse(token);
            Long id = Long.valueOf(claims.get("id").toString());
            String username = (String) claims.get("username");
            String role = (String) claims.get("role");
            JwtUtils.UserContext.set(new JwtUtils.LoginUser(id, username, role));
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\"}");
            return false;
        }
    }

    private boolean isPublic(String uri) {
        return uri.startsWith("/api/auth/")
                || uri.startsWith("/api/product/page")
                || uri.startsWith("/api/product/detail")
                || uri.startsWith("/api/product/hot")
                || uri.startsWith("/api/category/list")
                || uri.startsWith("/api/news/page")
                || uri.startsWith("/api/news/detail")
                || uri.startsWith("/api/banner/list")
                || uri.startsWith("/api/comment/page")
                || uri.startsWith("/uploads/");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        JwtUtils.UserContext.clear();
    }
}
