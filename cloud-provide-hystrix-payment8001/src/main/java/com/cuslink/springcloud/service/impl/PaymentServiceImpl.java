package com.cuslink.springcloud.service.impl;

import cn.hutool.core.util.IdUtil;
import com.cuslink.springcloud.service.PaymentService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 17:36
 * @description
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public String paymentInfo_OK(Integer id) {
        return "线程池："+ Thread.currentThread().getName()+"paymentInfo_OK,id"+id+"\t"+"O(∩_∩)O哈哈~";
    }

    @HystrixCommand(fallbackMethod = "paymentInfo_TimeOutHandle",commandProperties = {
            @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="5000")
    })
    public String paymentInfo_TimeOut(Integer id) {
        try {TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) { e.printStackTrace(); }
        return "线程池："+Thread.currentThread().getName()+"paymentInfo_TimeOut,id"+id+"\t"+"o(╥﹏╥)o,睡3秒~";
    }

    public String paymentInfo_TimeOutHandle(Integer id){
        return "系统繁忙请稍后再试...";
    }


    //服务熔断
    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallBack" ,commandProperties = {
            @HystrixProperty(name="circuitBreaker.enabled",value = "true"),//是否开启断路器
            @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value = "10"),//请求次数
            @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value = "10000"),//时间窗口期
            @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value = "60")//失败率达到多少后跳闸
    })
    public String paymentCircuitBreaker(@PathVariable("id")Integer id){
        if(id < 0){
            throw new RuntimeException("***id 不能为负数");
        }
       String number=  IdUtil.simpleUUID();
        return Thread.currentThread().getName()+"\t"+"调用成功，流水号"+number;
    }

    public String paymentCircuitBreakerFallBack(@PathVariable("id")Integer id){
            return "id不能为负数,o(╥﹏╥)o，"+id;
    }
}
