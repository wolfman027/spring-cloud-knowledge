package com.wolfman.marathon.controller;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import com.wolfman.marathon.dto.CustomerRequestParams;
import com.wolfman.marathon.feign.CustomerManagementClient;
import com.wolfman.marathon.service.CustomerInfoService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping(value = "/customer")
public class CustomerInfoController {

    @Autowired
    private CustomerManagementClient customerManagementClient;

    @Autowired
    private CustomerInfoService customerInfoService;

    @RequestMapping("/detail/{id}")
//    @Bulkhead(name = "backendA", fallbackMethod = "myBulkheadFallback", type = Bulkhead.Type.SEMAPHORE)
    public CustomerInfoDTO getCustomerDetailInfo(@PathVariable("id") String id) {
        System.out.println("request start: " + System.currentTimeMillis());
        CustomerInfoDTO customerInfoDTO = customerManagementClient.getCustomerInfo(id);
        log.info("customerInfoDTO:{}", customerInfoDTO);
        return customerInfoDTO;
    }

    public CustomerInfoDTO myBulkheadFallback(Throwable excetpion) {
        log.info(excetpion.getMessage());
        return CustomerInfoDTO.builder().build();
    }

    @RequestMapping("/detail/v2/{id}")
    public CustomerInfoDTO getCustomerDetailInfoV2(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        System.out.println("request start: " + System.currentTimeMillis());
        CustomerInfoDTO customerInfoDTO = customerInfoService.getCustomerDetailInfoV2(id).toCompletableFuture().get();
        log.info("customerInfoDTO:{}", customerInfoDTO);
        return customerInfoDTO;
    }

    @RequestMapping("/default-customer-name")
    public String defaultCustomerName(@RequestParam("name") String name) {
        String customerName = customerManagementClient.defaultCustomerName(name);
        log.info("customerName:{}", customerName);
        return customerName;
    }

    @RequestMapping("/check-existed")
    public String checkExisted(@RequestParam("name") String name,
                               @RequestParam("phone") String phone) {
        String result = customerManagementClient.checkExisted(CustomerRequestParams.builder().name(name).phone(phone).build());
        log.info("checkExisted result:{}", result);
        return result;
    }

}
