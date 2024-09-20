package com.wolfman.marathon.controller;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import com.wolfman.marathon.feign.CustomerManagementClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/customer")
public class CustomerInfoController {

    @Autowired
    private CustomerManagementClient customerManagementClient;

    @RequestMapping("/detail/{id}")
    public CustomerInfoDTO getCustomerDetailInfo(@PathVariable("id") String id) {
        CustomerInfoDTO customerInfoDTO = customerManagementClient.getCustomerInfo(id);
        log.info("customerInfoDTO:{}", customerInfoDTO);
        return customerInfoDTO;
    }

    @RequestMapping("/default-customer-name")
    public String defaultCustomerName(@RequestParam("name") String name) {
        String customerName = customerManagementClient.defaultCustomerName(name);
        log.info("customerName:{}", customerName);
        return customerName;
    }

}
