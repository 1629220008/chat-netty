package com.gateway.config;

import com.gateway.filter.LoginFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter loginFilter() {
        return new LoginFilter();
    }
}
