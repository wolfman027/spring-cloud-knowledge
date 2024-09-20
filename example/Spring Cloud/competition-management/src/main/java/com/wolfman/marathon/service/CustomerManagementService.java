package com.wolfman.marathon.service;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

public interface CustomerManagementService {

    @RequestMapping("/customer/{id}")
    CustomerInfoDTO getCustomerInfo(@PathVariable("id") String id);

}
