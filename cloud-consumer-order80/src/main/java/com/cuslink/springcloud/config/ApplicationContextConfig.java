package com.cuslink.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author wangzhikun
 * @date 2020/6/25 0025 13:19
 * @description
 */
@Configuration
public class ApplicationContextConfig {

    @Bean
//    @LoadBalanced //负载均衡 默认采用轮询的方式
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
