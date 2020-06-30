package com.cuslink.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 17:34
 * @description
 */
@SpringBootApplication
@EnableEurekaClient
public class HystrixPaymentMain8001 {
    
    public static void main(String[] args){
        SpringApplication.run(HystrixPaymentMain8001.class,args);
    }
}
