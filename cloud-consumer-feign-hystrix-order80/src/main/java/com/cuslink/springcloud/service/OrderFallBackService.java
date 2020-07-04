package com.cuslink.springcloud.service;

import org.springframework.stereotype.Component;

/**
 * @author wangzhikun
 * @date 2020/7/1 0001 16:21
 * @description
 */
@Component
public class OrderFallBackService implements OrderService{
    @Override
    public String ok(Integer id) {
        return "ok O(∩_∩)O哈哈~";
    }

    @Override
    public String timeOut(Integer id) {
        return "timeout o(╥﹏╥)o";
    }
}
