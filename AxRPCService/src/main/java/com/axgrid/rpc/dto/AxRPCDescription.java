package com.axgrid.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public interface AxRPCDescription {
    String getName();
    String getFullName();
    String getDescription();

    String getTrxFieldName();
    String getErrorCodeFieldName();
    String getCorrelationIdFieldName();
    String getSuccessFieldName();
    String getErrorTextFieldName();
    String getSessionFieldName();

    String getRequestObject();
    String getResponseObject();
    String getHttpEntryPoint();

    String getRequestObjectFullName();
    String getResponseObjectFullName();

    @JsonProperty("methods")
    List<AxRPCDescriptionMethod> getMethods();
}
