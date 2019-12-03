package com.axgrid.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AxRPCDescriptionMethod {
    String name;
    String requestFullName;
    String requestName;
    String responseFullName;
    String responseName;
    boolean loginRequired;
}
