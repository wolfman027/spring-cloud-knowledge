package com.wolfman.marathon.controller;

import com.wolfman.marathon.feign.CustomerManagementClient;
import com.wolfman.marathon.service.Resilience4JTestService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/resilience4j")
public class Resilience4JTestController {

    @Autowired
    private Resilience4JTestService resilience4JTestService;

    @Autowired
    private CustomerManagementClient customerManagementClient;

    @RequestMapping("/getOK")
    public String getCustomerDetailInfo() {
        return resilience4JTestService.getOK();
    }

    @RequestMapping("/getDefault")
    public String getDefaultOK() {
        return resilience4JTestService.getDefaultOK();
    }

    @RequestMapping("/getConcurrentOK")
    public String getConcurrentOK() {
        return resilience4JTestService.getConcurrentOK();
    }

    @RequestMapping("/checkCircuitBreaker")
    @Bulkhead(name = "backendA", fallbackMethod = "myBulkheadFallback", type = Bulkhead.Type.SEMAPHORE)
    public String checkCircuitBreaker() {
        return customerManagementClient.checkCircuitBreaker();
    }

    public String myBulkheadFallback(Throwable excetpion) {
        log.info(excetpion.getMessage());
        return "fallback";
    }

}
