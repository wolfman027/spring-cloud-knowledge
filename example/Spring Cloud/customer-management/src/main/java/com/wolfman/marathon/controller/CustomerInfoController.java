package com.wolfman.marathon.controller;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping(value = "/customer")
public class CustomerInfoController {

    @RequestMapping("/{id}")
    public CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id) throws InterruptedException {
        log.info("Getting customer info for id: {}", id);
//        Thread.sleep(10000);
        return CustomerInfoDTO.builder()
                .id(id)
                .customerName("John Doe")
                .build();
    }

    @RequestMapping("/default-customer-name")
    public String defaultCustomerName(HttpServletRequest request, @RequestParam("name") String name){
        for (String headerName : Collections.list(request.getHeaderNames())) {
            log.info("defaultCustomerName headers, key:{}, value:{}", headerName, request.getHeader(headerName));
        }
        log.info("Getting defaultCustomerName: {}", name);
        return "张三" + name;
    }

    @GetMapping("/check-existed")
    public String checkExisted(@RequestParam("name") String name,
                               @RequestParam("phone") String phone){
        log.info("checkExisted name:{}, phone:{}", name, phone);
        int i = new Random(100).nextInt();
        if (0 == i/0){
            log.info("i/0");
        }
        return "Y";
    }


    @GetMapping("/check-circuitbreaker")
    public String checkCircuitBreaker(){
        log.info("checkCircuitBreaker");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        int i = new Random(100).nextInt();
//        if (0 == i/0){
//            log.info("i/0");
//        }
        return "Y";
    }


}
