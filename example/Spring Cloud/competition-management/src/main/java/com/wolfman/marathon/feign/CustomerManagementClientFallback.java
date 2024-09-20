package com.wolfman.marathon.feign;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import com.wolfman.marathon.dto.CustomerRequestParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerManagementClientFallback implements CustomerManagementClient {
    @Override
    public CustomerInfoDTO getCustomerInfo(String id) {
        log.info("fallback");
        return null;
    }

    @Override
    public String defaultCustomerName(String name) {
        return null;
    }

    @Override
    public String checkExisted(CustomerRequestParams params) {
        return null;
    }

}
