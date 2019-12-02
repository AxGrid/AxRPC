package com.axgrid.rpc.web.service;

import com.axgrid.rpc.dto.AxRPCDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AxRPCWebService {

    @Autowired(required = false)
    List<AxRPCDescription> rpcDescriptions;

    public List<AxRPCDescription> getServices() {
        return null;
    }

}
