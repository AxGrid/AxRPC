package com.axgrid.rpc;

import lombok.Data;

@Data
public class AxContext implements AxRPCContext {
    boolean loggedIn = false;
    int counter = 0;
}
