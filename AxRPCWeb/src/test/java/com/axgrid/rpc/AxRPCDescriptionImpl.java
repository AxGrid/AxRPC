package com.axgrid.rpc;

import com.axgrid.rpc.dto.AxRPCDescription;
import com.axgrid.rpc.dto.AxRPCDescriptionMethod;
import lombok.Data;

import java.util.List;

@Data
public class AxRPCDescriptionImpl implements AxRPCDescription {

    String name;

    String fullName;


    String description;


    String trxFieldName;


    String errorCodeFieldName;


    String correlationIdFieldName;


    String successFieldName;


    String errorTextFieldName;


    String sessionFieldName;


    String requestObject;

    String responseObject;


    String httpEntryPoint;


    String requestObjectFullName;


    String responseObjectFullName;


    List<AxRPCDescriptionMethod> methods;
}
