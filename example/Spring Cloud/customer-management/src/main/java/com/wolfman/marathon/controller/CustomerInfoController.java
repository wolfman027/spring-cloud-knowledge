package com.wolfman.marathon.controller;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/customer")
public class CustomerInfoController {

    @RequestMapping("/{id}")
    public CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id){
        log.info("Getting customer info for id: {}", id);
        return CustomerInfoDTO.builder()
                .id(id)
                .customerName("John Doe")
                .build();
    }

    @RequestMapping("/default-customer-name")
    public String defaultCustomerName(@RequestParam("name") String name){
        log.info("Getting defaultCustomerName: {}", name);
        return "张三" + name;
    }

}
