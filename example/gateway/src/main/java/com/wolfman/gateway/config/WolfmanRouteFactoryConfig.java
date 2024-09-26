package com.wolfman.gateway.config;

import com.wolfman.gateway.route.WolfmanRoutePredicateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WolfmanRouteFactoryConfig {

    @Bean
    public WolfmanRoutePredicateFactory wolfmanRoutePredicateFactory() {
        return new WolfmanRoutePredicateFactory();
    }

}
