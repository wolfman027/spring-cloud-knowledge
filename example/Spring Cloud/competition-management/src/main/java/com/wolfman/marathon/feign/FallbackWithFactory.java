package com.wolfman.marathon.feign;

import com.wolfman.marathon.dto.CustomerInfoDTO;

public class FallbackWithFactory implements CustomerManagementClient {

    @Override
    public CustomerInfoDTO getCustomerInfo(String id) {
        return new CustomerInfoDTO();
    }

    @Override
    public String defaultCustomerName(String name) {
        return null;
    }

}
