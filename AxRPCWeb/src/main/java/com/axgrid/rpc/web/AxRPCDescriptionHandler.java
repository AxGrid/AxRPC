package com.axgrid.rpc.web;

import com.axgrid.rpc.dto.AxRPCDescription;
import com.axgrid.rpc.service.AxRPCWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ax-rpc/v1/")
public class AxRPCDescriptionHandler {

    @Autowired
    AxRPCWebService service;

    @GetMapping("/description.json")
    List<AxRPCDescription> services() {
        return service.getServices();
    }

}
