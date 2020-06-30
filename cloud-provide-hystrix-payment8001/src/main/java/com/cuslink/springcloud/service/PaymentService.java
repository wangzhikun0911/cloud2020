package com.cuslink.springcloud.service;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 17:35
 * @description
 */
public interface PaymentService {


    String paymentInfo_OK(Integer id);

    String paymentInfo_TimeOut(Integer id);
}
