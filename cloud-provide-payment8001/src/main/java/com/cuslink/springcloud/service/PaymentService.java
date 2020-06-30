package com.cuslink.springcloud.service;

import com.cuslink.springcloud.entities.Payment;

/**
 * @author：wangzhikun
 * @date： 2020/6/24 002414:14
 * @description：
 */
public interface PaymentService {
    int create(Payment payment);

    Payment getPaymentById(Long id);
}
