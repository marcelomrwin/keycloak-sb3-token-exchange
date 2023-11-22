package com.redhat.rhbk.conf;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionType;
import org.infinispan.spring.starter.embedded.InfinispanCacheConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenCacheConfiguration {
public static final String TOKEN_CACHE_NAME = "jwtCache";

    @Bean
    public InfinispanCacheConfigurer cacheConfigurer() {
        return manager -> {
            final org.infinispan.configuration.cache.Configuration ispnConfig = new ConfigurationBuilder()
                    .clustering()
                    .cacheMode(CacheMode.LOCAL)
                    .memory().size(1000L)
                    .evictionType(EvictionType.COUNT)
                    .build();
            manager.defineConfiguration(TOKEN_CACHE_NAME, ispnConfig);
        };
    }
}
