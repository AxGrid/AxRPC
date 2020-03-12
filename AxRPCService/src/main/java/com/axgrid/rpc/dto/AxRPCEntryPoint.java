package com.axgrid.rpc.dto;

import com.google.protobuf.GeneratedMessageV3;

public interface AxRPCEntryPoint<T extends GeneratedMessageV3> extends AxRPCUniversalEntryPoint {
    EntryPointTypes getType();
}
