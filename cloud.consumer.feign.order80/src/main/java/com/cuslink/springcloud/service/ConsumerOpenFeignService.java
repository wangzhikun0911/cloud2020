package com.cuslink.springcloud.service;

import com.cuslink.springcloud.entities.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 12:30
 * @description
 */
@Component
@FeignClient(value = "CLOUD-PAYMENT-SERVICE")
public interface ConsumerOpenFeignService {

    @GetMapping("/payment/getPaymentById/{id}")
    CommonResult getPaymentById(@PathVariable("id") Long id);

    @GetMapping("/payment/timeout")
    String getFeignTimeout();
}
