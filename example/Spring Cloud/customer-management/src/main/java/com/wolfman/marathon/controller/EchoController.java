package com.wolfman.marathon.controller;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EchoController {

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @GetMapping("/zone")
    public String zone() {
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        return "provider zone " + metadata.get("zone");
    }

}
