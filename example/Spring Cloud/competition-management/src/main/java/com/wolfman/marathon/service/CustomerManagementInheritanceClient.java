package com.wolfman.marathon.service;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("customer-management-inherit")
public interface CustomerManagementInheritanceClient extends CustomerManagementService {
}
