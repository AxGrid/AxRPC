package com.axgrid.rpc.web.handler;

import com.axgrid.rpc.dto.AxRPCDescription;

import com.axgrid.rpc.web.service.AxRPCWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/ax-rpc/v1/")
public class AxRPCDescriptionHandler {

    @Autowired
    AxRPCWebService service;

    @GetMapping("/description.json")
    List<AxRPCDescription> services(@RequestParam(required = false, defaultValue = "") String requestObject, @RequestParam(required = false, defaultValue = "") String responseObject) {
        if (requestObject.equals("") && responseObject.equals(""))
            return service.getServices();
        Stream<AxRPCDescription> stream = service.getServices().stream();
        if (!requestObject.equals(""))
            stream = stream.filter(item -> item.getRequestObjectFullName().equals(requestObject));
        if (!responseObject.equals(""))
            stream = stream.filter(item -> item.getResponseObjectFullName().equals(responseObject));
        return stream.collect(Collectors.toList());
    }

}
