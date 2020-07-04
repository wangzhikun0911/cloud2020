package com.cuslink.springcloud.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author wangzhikun
 * @date 2020/7/1 0001 10:09
 * @description
 */
@Component
@FeignClient(value = "CLOUD-PROVIDE-HYSTRIX-PAYMENT",fallback = OrderFallBackService.class)
public interface OrderService {

    @GetMapping("/payment/hystrix/ok/{id}")
    String ok(@PathVariable("id") Integer id);


    @GetMapping("/payment/hystrix/timeOut/{id}")
    String timeOut(@PathVariable("id") Integer id);
}
