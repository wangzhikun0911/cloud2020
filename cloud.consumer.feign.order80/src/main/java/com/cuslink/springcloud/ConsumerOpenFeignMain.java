package com.cuslink.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 12:29
 * @description
 */
@SpringBootApplication
@EnableFeignClients
public class ConsumerOpenFeignMain {
    public static void main(String[] args){
        SpringApplication.run(ConsumerOpenFeignMain.class,args);
    }
}
