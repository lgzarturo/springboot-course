package com.lgzarturo.springbootcourse.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager(USERS_CACHE).apply {
            setCaffeine(
                Caffeine.newBuilder()
                    .expireAfterWrite(USERS_CACHE_TTL_SECONDS, TimeUnit.SECONDS)
                    .maximumSize(USERS_CACHE_MAX_SIZE),
            )
        }

    companion object {
        const val USERS_CACHE = "users"
        const val USERS_CACHE_TTL_SECONDS = 60L
        const val USERS_CACHE_MAX_SIZE = 10_000L
    }
}
