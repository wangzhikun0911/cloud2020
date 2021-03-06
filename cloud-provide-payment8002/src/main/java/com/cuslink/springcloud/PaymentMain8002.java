package com.cuslink.springcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author：wangzhikun
 * @date： 2020/6/23 14:55
 * @description：
 */
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@Slf4j
public class PaymentMain8002 {
    public static void main(String[] args){
        SpringApplication.run(PaymentMain8002.class,args);
        log.info("-----------------------8002启动成功-----------------------");
    }
}
