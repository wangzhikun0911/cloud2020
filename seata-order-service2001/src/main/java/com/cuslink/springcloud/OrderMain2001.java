package com.cuslink.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 19:58
 * @description
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动创建的配置
@EnableDiscoveryClient
@EnableFeignClients
public class OrderMain2001 {
    public static void main(String[] args){
        SpringApplication.run(OrderMain2001.class,args);
    }
}
