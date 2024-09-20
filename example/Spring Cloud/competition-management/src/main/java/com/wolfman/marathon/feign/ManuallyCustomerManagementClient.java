package com.wolfman.marathon.feign;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

public interface ManuallyCustomerManagementClient {

    @RequestMapping("/customer/{id}")
    CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id);

}
