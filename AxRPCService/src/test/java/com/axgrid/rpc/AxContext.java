package com.axgrid.rpc;

import com.axgrid.rpc.dto.AxRPCContext;
import lombok.Data;

@Data
public class AxContext implements AxRPCContext {
    boolean loggedIn = false;
    int counter = 0;
}
