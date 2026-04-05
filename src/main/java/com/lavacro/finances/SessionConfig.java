package com.lavacro.finances;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

@Configuration
@EnableRedisIndexedHttpSession(maxInactiveIntervalInSeconds = 600, redisNamespace = "finances")
public class SessionConfig {
}
