package com.axgrid.rpc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

public interface AxRPCDescription {
    String getName();
    String getFullName();

    String getErrorCodeFieldName();
    String getCorrelationIdFieldName();
    String getSuccessFieldName();
    String getErrorTextFieldName();
    String getSessionFieldName();

    String getRequestObject();
    String getResponseObject();

    String getRequestObjectFullName();
    String getResponseObjectFullName();

    @JsonProperty("methods")
    List<AxRPCDescriptionMethod> getDescription();
}
