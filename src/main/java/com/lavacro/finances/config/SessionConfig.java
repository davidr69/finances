package com.lavacro.finances.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 600, redisNamespace = "finances")
public class SessionConfig {

    /**
     * Configure RedisTemplate for session validation
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * Filter to validate that sessions exist in Redis before allowing access.
     * If a session key is deleted from Redis, the user will be logged out.
     */
    @Bean
    public SessionValidationFilter sessionValidationFilter(RedisTemplate<String, Object> redisTemplate) {
        return new SessionValidationFilter(redisTemplate);
    }

    /**
     * Filter that validates session existence in Redis.
     * If the session is missing from Redis, it invalidates the session cookie.
     */
    public class SessionValidationFilter extends OncePerRequestFilter {

        private final RedisTemplate<String, Object> redisTemplate;

        public SessionValidationFilter(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            // Get session ID from JSESSIONID cookie
            String sessionId = getSessionIdFromCookie(request);

            // If session ID exists, validate it exists in Redis
            if (sessionId != null && !sessionId.isEmpty()) {
                String redisKey = "finances:sessions:" + sessionId;
                Boolean sessionExists = redisTemplate.hasKey(redisKey);

                // If session doesn't exist in Redis, invalidate the cookie
                if (Boolean.FALSE.equals(sessionExists)) {
                    // Invalidate the session cookie
                    invalidateSessionCookie(response);

                    // Redirect to login if it's not already a public endpoint
                    String requestURI = request.getRequestURI();
                    if (!isPublicEndpoint(requestURI)) {
                        response.sendRedirect(request.getContextPath() + "/login.html");
                        return;
                    }
                }
            }

            filterChain.doFilter(request, response);
        }

        /**
         * Extract session ID from JSESSIONID cookie
         */
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

        /**
         * Invalidate the JSESSIONID cookie
         */
        private void invalidateSessionCookie(HttpServletResponse response) {
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        /**
         * Check if the endpoint is public (doesn't require authentication)
         */
        private boolean isPublicEndpoint(String requestURI) {
            return requestURI.endsWith("/login.html")
                    || requestURI.endsWith("/authenticate")
                    || requestURI.endsWith("/css/**")
                    || requestURI.endsWith("/js/**")
                    || requestURI.endsWith("/font-awesome-4.7.0/**")
                    || requestURI.endsWith("/favicon.ico")
                    || requestURI.equals("/")
                    || requestURI.isEmpty();
        }
    }
}
