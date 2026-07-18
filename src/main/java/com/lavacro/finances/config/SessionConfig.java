package com.lavacro.finances.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 600, redisNamespace = "finances")
public class SessionConfig {

    @Bean
    public SessionValidationFilter sessionValidationFilter() {
        return new SessionValidationFilter();
    }

    @Bean
    public FilterRegistrationBean<SessionValidationFilter> sessionValidationFilterRegistration(
            SessionValidationFilter filter) {
        FilterRegistrationBean<SessionValidationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    public static class SessionValidationFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            if (isPublicEndpoint(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String sessionId = getSessionIdFromCookie(request);
            if (sessionId != null) {
                HttpSession session = request.getSession(false);
                if (session == null) {
                    invalidateSessionCookie(response);
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/login.html");
                    }
                    return;
                }
            }

            filterChain.doFilter(request, response);
        }

        private String getSessionIdFromCookie(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JSESSIONID".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        }

        private void invalidateSessionCookie(HttpServletResponse response) {
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        private boolean isPublicEndpoint(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.equals("/login.html") ||
                   path.equals("/authenticate") ||
                   path.startsWith("/css/") ||
                   path.startsWith("/js/") ||
                   path.startsWith("/font-awesome-4.7.0/") ||
                   path.equals("/favicon.ico");
        }
    }
}
