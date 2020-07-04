package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.service.OrderService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/7/1 0001 10:18
 * @description
 */
@RestController
@Slf4j
@DefaultProperties(defaultFallback = "timeoutConsumer2")
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/cosumer/order/hystrix/ok/{id}")
    public String ok(@PathVariable("id") Integer id){
       String result = orderService.ok(id);
       log.info("result="+result);
       return result;
    }



//    @HystrixCommand(fallbackMethod = "timeoutConsumer" ,commandProperties = {
//            @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value = "1500")
//    })
    @GetMapping("/cosumer/order/hystrix/timeout/{id}")
    @HystrixCommand
    public String timeout(@PathVariable("id") Integer id){
//        int i = 10/0;
        String result = orderService.timeOut(id);
        log.info("result="+result);
        return  result;
    }

    public String timeoutConsumer(@PathVariable("id") Integer id){
        return "80服务崩溃了...";
    }

    public String timeoutConsumer2(){
        return "80服务崩溃了...o(╥﹏╥)o";
    }
}
