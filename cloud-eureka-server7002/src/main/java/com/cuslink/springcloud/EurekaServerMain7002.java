package com.cuslink.springcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author wangzhikun
 * @date 2020/6/26 0026 15:35
 * @description
 */
@SpringBootApplication
@EnableEurekaServer
@Slf4j
public class EurekaServerMain7002 {
    public static void main(String[] args){
        SpringApplication.run(EurekaServerMain7002.class,args);
    }
}
