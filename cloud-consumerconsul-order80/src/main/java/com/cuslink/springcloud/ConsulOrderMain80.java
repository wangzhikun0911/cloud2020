package com.cuslink.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author wangzhikun
 * @date 2020/6/27 0027 15:02
 * @description
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ConsulOrderMain80 {
    public static void main(String[] args){
        SpringApplication.run(ConsulOrderMain80.class,args);
    }
}
