package com.cuslink.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * @author wangzhikun
 * @date 2020/7/1 0001 20:53
 * @description
 */
@SpringBootApplication
@EnableHystrixDashboard //表明开启dashboard
public class HystrixDashboardMain9001 {
    public static void main(String[] args){
        SpringApplication.run(HystrixDashboardMain9001.class,args);
    }
}
