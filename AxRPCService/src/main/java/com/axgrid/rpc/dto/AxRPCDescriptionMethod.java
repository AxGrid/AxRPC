package com.axgrid.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AxRPCDescriptionMethod {
    String requestObject;
    String responseObject;
    boolean loginRequired;
}
