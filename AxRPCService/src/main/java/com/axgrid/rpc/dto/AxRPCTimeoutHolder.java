package com.axgrid.rpc.dto;

import com.axgrid.rpc.AxRPCTimeout;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AxRPCTimeoutHolder {
    int timeout;
    int retry = 5;
    int timeoutRetry = 50;

    public AxRPCTimeoutHolder(int timeout) {
        this.timeout = timeout;
    }

    public AxRPCTimeoutHolder(AxRPCTimeout t) {
        this.timeout = t.value();
        this.retry = t.retry();
        this.timeoutRetry = t.retryTimeout();
    }
}
