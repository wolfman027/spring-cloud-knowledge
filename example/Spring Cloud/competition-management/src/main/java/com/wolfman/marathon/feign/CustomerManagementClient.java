package com.wolfman.marathon.feign;

import com.wolfman.marathon.config.FeignLogConfig;
import com.wolfman.marathon.dto.CustomerInfoDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "customer-management", contextId = "customerManagementClient",
//        fallback = CustomerManagementClientFallback.class,
        fallbackFactory = CustomerManagementClientFallbackFactory.class,
        configuration = FeignLogConfig.class
)
public interface CustomerManagementClient {

    @RequestMapping("/customer/{id}")
    CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id);

    @GetMapping("/customer/default-customer-name")
    @Cacheable(cacheNames = "demo-cache", key = "#name")
    String defaultCustomerName(@RequestParam("name") String name);

}
