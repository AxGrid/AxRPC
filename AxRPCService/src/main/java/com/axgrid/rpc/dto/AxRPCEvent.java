package com.axgrid.rpc.dto;

import com.google.protobuf.GeneratedMessageV3;
import lombok.Data;

@Data
public class AxRPCEvent<V extends GeneratedMessageV3, C extends AxRPCContext> {
    C context;
    String channel;
    V message;
}
