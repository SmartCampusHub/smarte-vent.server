package com.winnguyen1905.Activity.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisConfig {

  @Bean
  ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
    return new LettuceConnectionFactory("localhost", 6379);
  }

  @Bean
  ReactiveRedisTemplate<UUID, UUID> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
    Jackson2JsonRedisSerializer<UUID> serializer = new Jackson2JsonRedisSerializer<>(UUID.class);
    RedisSerializationContext.RedisSerializationContextBuilder<UUID, UUID> builder = RedisSerializationContext
        .newSerializationContext(new Jackson2JsonRedisSerializer<>(UUID.class));
    RedisSerializationContext<UUID, UUID> context = builder.value(serializer).hashValue(serializer)
        .hashKey(serializer).build(); 
    return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
  }

}
