package com.tradeledger.cards.config;

import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Created by daqwang on 5/8/21.
 */
@Configuration
public class WebClientConfig {

    @Value("${third.party.endpoint}")
    private String baseUrl;

    @Bean
    public WebClient thirdPartyWebClient(){
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(WebClientFilter.logResponse())
                .build();
    }

    @Bean
    @Primary
    @Order(value= Ordered.HIGHEST_PRECEDENCE)
    public HystrixCommandAspect hystrixAspect() {
        return new HystrixCommandAspect();
    }
}
