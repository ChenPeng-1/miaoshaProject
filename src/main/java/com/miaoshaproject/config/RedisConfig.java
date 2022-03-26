package com.miaoshaproject.config;

import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.stereotype.Component;

@Component
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)//session存储时间设置为一小时
public class RedisConfig {
}
