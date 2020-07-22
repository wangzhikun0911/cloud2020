package com.cuslink.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:25
 * @description
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AccountMain2003 {

    public static void main(String[] args) {
        SpringApplication.run(AccountMain2003.class, args);
    }
}
