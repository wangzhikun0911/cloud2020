package com.cuslink.springcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author wangzhikun
 * @date 2020/6/25 0025 13:16
 * @description
 */
@SpringBootApplication
@EnableEurekaClient
//@RibbonClient(name="cloud-payment-service",configuration = MyIRule.class)
@Slf4j
public class OrderMain80 {
    public static void main(String[] args){
        SpringApplication.run(OrderMain80.class,args);
        log.info("-----------------------80启动成功-----------------------");
    }
}
