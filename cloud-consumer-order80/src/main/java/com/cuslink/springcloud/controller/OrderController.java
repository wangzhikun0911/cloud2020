package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.entities.CommonResult;
import com.cuslink.springcloud.entities.Payment;
import com.cuslink.springcloud.lb.LoadBalance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;

/**
 * @author wangzhikun
 * @date 2020/6/25 0025 13:18
 * @description
 */
@RestController
public class OrderController {

//    public static final String PAYMENT_URL = "http://localhost:8081/";
    //通过在eureka上注册过的服务名称调用
    public static final String PAYMENT_URL = "http://cloud-payment-service/";

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private LoadBalance loadBalance;

    @Resource
    private DiscoveryClient discoveryClient;

    @GetMapping("/consumer/payment/create")
    public CommonResult<Payment> create(Payment payment) {
        return restTemplate.postForObject(PAYMENT_URL + "payment/create", payment, CommonResult.class);
    }

    @GetMapping("/consumer/payment/getPaymentById/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id) {
        return restTemplate.getForObject(PAYMENT_URL + "payment/getPaymentById/" + id, CommonResult.class);
    }

    @GetMapping("/consumer/payment/lb")
    public String getServerPort(){
        List<ServiceInstance> instances = discoveryClient.getInstances("cloud-payment-service");
        if(instances.size() == 0 || instances == null){
            return null;
        }
        ServiceInstance serviceInstance = loadBalance.getServiceInstance(instances);
        URI uri = serviceInstance.getUri();
        return  restTemplate.getForObject(uri+"/payment/lb",String.class);
    }

    @GetMapping("/consumer/payment/zipkin")
    public String paymentZipkin() {
        String result = restTemplate.getForObject("http://localhost:8001" + "/payment/zipkin/", String.class);
        return result;
    }

}
