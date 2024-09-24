package com.wolfman.marathon.feign;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import com.wolfman.marathon.dto.CustomerRequestParams;

import java.time.LocalTime;

public class FallbackWithFactory implements CustomerManagementClient {

    @Override
    public CustomerInfoDTO getCustomerInfo(String id) {
        System.out.println("fallback" + System.currentTimeMillis());
        return new CustomerInfoDTO();
    }

    @Override
    public String defaultCustomerName(String name) {
        return null;
    }

    @Override
    public String checkExisted(CustomerRequestParams params) {
        return null;
    }

    @Override
    public String checkCircuitBreaker() {
        System.out.println("checkCircuitBreaker failback" + System.currentTimeMillis());
        return null;
    }

}
