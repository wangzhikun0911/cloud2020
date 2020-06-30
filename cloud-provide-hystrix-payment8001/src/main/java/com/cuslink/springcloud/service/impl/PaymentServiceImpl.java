package com.cuslink.springcloud.service.impl;

import com.cuslink.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 17:36
 * @description
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Override
    public String paymentInfo_OK(Integer id) {
        return "线程池："+ Thread.currentThread().getName()+"paymentInfo_OK,id"+id+"\t"+"O(∩_∩)O哈哈~";
    }

    @Override
    public String paymentInfo_TimeOut(Integer id) {
        try {TimeUnit.SECONDS.sleep(3);} catch (InterruptedException e) { e.printStackTrace(); }
        return "线程池："+Thread.currentThread().getName()+"paymentInfo_TimeOut,id"+id+"\t"+"O(∩_∩)O哈哈,睡3秒~";
    }
}
