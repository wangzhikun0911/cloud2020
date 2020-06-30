package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.entities.CommonResult;
import com.cuslink.springcloud.service.ConsumerOpenFeignService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 12:33
 * @description
 */
@RestController
public class ConsumerOpenFeignController {
    @Resource
    private ConsumerOpenFeignService consumerOpenFeignService;

    @GetMapping("/consumer/payment/getPaymentById/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id){
       return  consumerOpenFeignService.getPaymentById(id);
    }

    @GetMapping("/consumer/payment/timeout")
    public String getFeignTimeout(){
        return consumerOpenFeignService.getFeignTimeout();
    }
}
