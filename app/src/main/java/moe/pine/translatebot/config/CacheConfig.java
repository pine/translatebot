package moe.pine.translatebot.config;

import moe.pine.spring.cache.interceptors.CacheInterceptor;
import moe.pine.spring.cache.interceptors.CachePolicyBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean
    public CacheInterceptor noCacheInterceptor() {
        final var cachePolicy = new CachePolicyBuilder()
                .private_()
                .noCache()
                .noStore()
                .mustRevalidate()
                .build();

        return new CacheInterceptor(cachePolicy);
    }
}
