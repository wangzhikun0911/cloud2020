package com.cuslink.springcloud.service.impl;

import com.cuslink.springcloud.dao.PaymentDao;
import com.cuslink.springcloud.entities.Payment;
import com.cuslink.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author：wangzhikun
 * @date： 2020/6/24 002414:15
 * @description：
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Resource
    private PaymentDao paymentDao;

    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }
}
