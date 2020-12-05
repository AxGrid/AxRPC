package com.axgrid.rpc.web.service;

import com.axgrid.rpc.dto.AxRPCDescription;
import com.axgrid.rpc.dto.AxRPCEventDescription;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
public class AxRPCWebService {

    @Autowired(required = false)
    @Getter
    List<AxRPCDescription> rpcDescriptions;

    @Autowired(required = false)
    @Getter
    List<AxRPCEventDescription> eventDescriptions;


}
