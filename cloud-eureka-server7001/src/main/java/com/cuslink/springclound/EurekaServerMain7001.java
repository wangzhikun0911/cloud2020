package com.cuslink.springclound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author wangzhikun
 * @date 2020/6/26 0026 14:59
 * @description
 */
@SpringBootApplication
@EnableEurekaServer
@Slf4j
public class EurekaServerMain7001 {
    public static void main(String[] args){
        SpringApplication.run(EurekaServerMain7001.class,args);
        log.info("----------------------注册服务7001启动----------------------");
    }
}
