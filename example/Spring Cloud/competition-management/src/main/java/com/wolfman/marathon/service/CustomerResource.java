package com.wolfman.marathon.service;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerResource implements CustomerManagementService {

    @Override
    public CustomerInfoDTO getCustomerInfo(String id) {
        return null;
    }

}
