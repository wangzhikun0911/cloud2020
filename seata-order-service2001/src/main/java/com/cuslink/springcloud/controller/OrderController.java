package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.domain.CommonResult;
import com.cuslink.springcloud.domain.Order;
import com.cuslink.springcloud.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 19:50
 * @description
 */
@RestController
public class OrderController {

    @Resource
    private OrderService orderService;

    @GetMapping("/order/create")
    public CommonResult create(Order order){
        orderService.createOrder(order);
        return new CommonResult(200,"下单成功");
    }
}
