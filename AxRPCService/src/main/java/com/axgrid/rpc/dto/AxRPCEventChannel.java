package com.axgrid.rpc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Канал сообщений
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AxRPCEventChannel {

    @EqualsAndHashCode.Include
    String channelId;

    long updateTime = 0;
    public boolean isTimeout() { return false; }

    public AxRPCEventChannel(String channelId) {
        this.setChannelId(channelId);
    }

}
