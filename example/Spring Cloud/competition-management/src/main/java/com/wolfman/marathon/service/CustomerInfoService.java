package com.wolfman.marathon.service;

import com.wolfman.marathon.dto.CustomerInfoDTO;
import com.wolfman.marathon.feign.CustomerManagementClient;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Slf4j
@Service
public class CustomerInfoService {

    @Autowired
    private CustomerManagementClient customerManagementClient;

    @TimeLimiter(name = "backendA")
    public CompletionStage<CustomerInfoDTO> getCustomerDetailInfoV2(@PathVariable("id") String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(30000); // 模拟长时间运行的操作
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return CustomerInfoDTO.builder().build();
        });
    }


}
