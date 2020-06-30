package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 17:46
 * @description
 */
@RestController
@Slf4j
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/payment/hystrix/ok/{id}")
    public String ok(@PathVariable("id") Integer id){
        String result = paymentService.paymentInfo_OK(id);
        log.info("******result="+ result );
        return result;
    }

    @GetMapping("/payment/hystrix/timeOut/{id}")
    public String timeOut(@PathVariable("id") Integer id){
        String result =  paymentService.paymentInfo_TimeOut(id);
        log.info("******result="+ result );
        return serverPort;
    }
}
