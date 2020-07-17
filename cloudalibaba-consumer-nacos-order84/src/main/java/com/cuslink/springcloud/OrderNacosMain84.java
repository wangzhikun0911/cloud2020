package com.cuslink.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author wangzhikun
 * @date 2020/7/17 0017 23:47
 * @description
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OrderNacosMain84 {
    public static void main(String[] args){
        SpringApplication.run(OrderNacosMain84.class,args);
    }
}
