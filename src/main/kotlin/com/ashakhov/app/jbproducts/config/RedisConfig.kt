package com.ashakhov.app.jbproducts.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableRedisRepositories
class RedisConfig {

    /**
     * Connection factory bean to connect to Redis
     * @return {@link JedisConnectionFactory}
     */
    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration()
        redisStandaloneConfiguration.hostName = "localhost"
        redisStandaloneConfiguration.port = 16379
        return JedisConnectionFactory(redisStandaloneConfiguration)
    }
}