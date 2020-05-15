package com.axgrid.rpc.repository;

import com.axgrid.rpc.AxRPCServiceConfiguration;
import com.google.protobuf.GeneratedMessageV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class AxRPCCache<V extends GeneratedMessageV3> {

    @CachePut(value = AxRPCServiceConfiguration.RPC_CACHE, key="#trx", condition = "#trx != null && #trx.length() > 5", unless = "#result == null")
    public V put(String trx, V result) {
        if (log.isDebugEnabled()) log.debug("Put into Cache:{} Trx:{} Value:{}", AxRPCServiceConfiguration.RPC_CACHE, trx, result);
        return result;
    }

    @Cacheable(value = AxRPCServiceConfiguration.RPC_CACHE, key="#trx", condition = "#trx != null && #trx.length() > 5", unless = "#result == null")
    public V get(String trx) {
        if (log.isDebugEnabled()) log.debug("Not in Cache:{} Trx:{}", AxRPCServiceConfiguration.RPC_CACHE, trx);
        return null;
    }

    @Cacheable(value = AxRPCServiceConfiguration.RPC_CACHE, key="#trx", condition = "#trx != null && #trx.length() > 5", unless = "#result == null")
    public V putOrGet(String trx, V result) {
        if (log.isDebugEnabled()) log.debug("PutOrGet into Cache:{} Trx:{} Value:{}", AxRPCServiceConfiguration.RPC_CACHE, trx, result);
        return result;
    }
}
