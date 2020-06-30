package com.cuslink.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author wangzhikun
 * @date 2020/6/27 0027 14:44
 * @description
 */
@RestController
public class PaymentController {
    @Value("${server.port}")
    private String servetPort;


    @RequestMapping("/payment/consul")
    public String paymentConsul(){
        return "spring cloud consul:" + servetPort+"\t" + UUID.randomUUID().toString();
    }
}
