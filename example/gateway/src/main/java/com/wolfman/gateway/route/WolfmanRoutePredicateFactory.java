package com.wolfman.gateway.route;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class WolfmanRoutePredicateFactory extends AbstractRoutePredicateFactory<WolfmanRoutePredicateFactory.Config> {

    /**
     * Header key.
     */
    public static final String HEADER_KEY = "headerKey";

    /**
     * Regexp key.
     */
    public static final String HEADER_VALUE = "headerValue";


    public WolfmanRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(HEADER_KEY, HEADER_VALUE);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {

        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange exchange) {
                if (exchange.getRequest().getHeaders().get(config.headerKey) == null){
                    return false;
                }
                List<String> values = exchange.getRequest().getHeaders().getOrDefault(config.headerKey,
                        Collections.emptyList());
                return values.contains(config.getHeaderValue());
            }

            @Override
            public Object getConfig() {
                return config;
            }

            @Override
            public String toString() {
                return String.format("headerKey: %s headerValue=%s", config.headerKey, config.headerValue);
            }

        };
    }


    @Validated
    public static class Config {

        @NotEmpty
        private String headerKey;

        private String headerValue;

        public String getHeaderKey() {
            return headerKey;
        }

        public Config setHeaderKey(String header) {
            this.headerKey = header;
            return this;
        }

        public String getHeaderValue() {
            return headerValue;
        }

        public Config setHeaderValue(String headerValue) {
            this.headerValue = headerValue;
            return this;
        }

    }

}
