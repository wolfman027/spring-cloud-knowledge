package com.wolfman.marathon.feign;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "customer-management", url = "http://localhost:9090")
public interface CustomerManagementClient {

    @RequestMapping("/customer/{id}")
    CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id);

}
