package com.axgrid.rpc.web.handler;

import com.axgrid.rpc.dto.AxRPCDescription;

import com.axgrid.rpc.dto.AxRPCEventDescription;
import com.axgrid.rpc.web.service.AxRPCWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
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
            return service.getRpcDescriptions();
        Stream<AxRPCDescription> stream = service.getRpcDescriptions().stream();
        if (!requestObject.equals(""))
            stream = stream.filter(item -> item.getRequestObjectFullName().equals(requestObject));
        if (!responseObject.equals(""))
            stream = stream.filter(item -> item.getResponseObjectFullName().equals(responseObject));
        return stream.collect(Collectors.toList());
    }

    @GetMapping("/event.json")
    List<AxRPCEventDescription> events(@RequestParam(required = false, defaultValue = "") String eventObject) {
        if (eventObject.equals(""))
            return service.getEventDescriptions();
        return service.getEventDescriptions().stream()
                .filter(item -> item.getEventObjectFullName().equals(eventObject))
                .collect(Collectors.toList());
    }
}
