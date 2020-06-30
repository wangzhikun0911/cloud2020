package com.cuslink.springcloud.controller;


import com.cuslink.springcloud.entities.CommonResult;
import com.cuslink.springcloud.entities.Payment;
import com.cuslink.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author：wangzhikun
 * @date： 2020/6/24 002414:16
 * @description：
 */
@RestController
@Slf4j
public class PaymentController {

    @Value("${server.port}")
    private String serverPort;

    @Resource
    private PaymentService paymentService;

    @Resource
    private DiscoveryClient discoveryClient;

    @PostMapping("/payment/create")
    public CommonResult create(@RequestBody Payment payment) {
        int result = paymentService.create(payment);
        log.info("插入结果：" + result);
        if (result > 0) {
            return new CommonResult(200, "插入成功！", result);
        } else {
            return new CommonResult(444, "插入失败！", null);
        }
    }

    @GetMapping("/payment/getPaymentById/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id) {
        Payment payment = paymentService.getPaymentById(id);
        log.info("查询结果：" + payment);
        if (payment != null) {
            return new CommonResult(200, "查询成功！serverPort:"+serverPort, payment);
        } else {
            return new CommonResult(444, "没有查询记录，查询ID：！" + id, null);
        }
    }

    @GetMapping("/payment/disconvery")
    public Object disconvery(){
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            log.info("**************service:" + service);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        for (ServiceInstance instance : instances) {
            log.info("****"+instance.getServiceId()+"\t"+instance.getHost()+"\t"+instance.getPort()+"\t"+instance.getUri());
        }
        return this.discoveryClient;
    }
    @GetMapping("/payment/lb")
    public String serverPort(){
        return serverPort;
    }
}
