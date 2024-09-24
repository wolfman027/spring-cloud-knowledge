package com.wolfman.marathon.controller;

import com.wolfman.marathon.feign.ManuallyCustomerManagementClient;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.CachingCapability;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;


//@Import(FeignClientsConfiguration.class)
//@RestController
class FooController {

    private ManuallyCustomerManagementClient fooClient;

    private ManuallyCustomerManagementClient adminClient;

//    @Autowired
    public FooController(Client client, Encoder encoder, Decoder decoder, Contract contract, CachingCapability capability) {
        this.fooClient = Feign.builder().client(client)
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract)
                .addCapability(capability)
                .requestInterceptor(new BasicAuthRequestInterceptor("user", "user"))
                .target(ManuallyCustomerManagementClient.class, "https://customer-management");

        this.adminClient = Feign.builder().client(client)
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract)
                .addCapability(capability)
                .requestInterceptor(new BasicAuthRequestInterceptor("admin", "admin"))
                .target(ManuallyCustomerManagementClient.class, "https://customer-management");
    }
}