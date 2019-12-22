package com.axgrid.rpc;

import com.axgrid.cache.AxCacheObject;
import com.axgrid.cache.EnableAxCache;
import com.axgrid.metrics.EnableAxMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@EnableAxCache
@EnableAxMetrics
public class AxRPCServiceConfiguration {

    public static final String RPC_CACHE ="rpc-trx";

    @Bean
    public AxCacheObject getMyCache(@Value("${axgrid.rpc.cache.expire:1800}") int userExpireAfterAccess,
                                    @Value("${axgrid.rpc.cache.size:100000}") int userSize)
    {
        return AxCacheObject
                .builder()
                .configuration(new AxCacheObject.CacheObjectConfiguration(RPC_CACHE,
                        AxCacheObject.ExpireType.Write,
                        userExpireAfterAccess,
                        userSize))
                .build();
    }

}
