package com.cuslink.springcloud.dao;


import com.cuslink.springcloud.entities.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author：wangzhikun
 * @date： 2020/6/24 002414:12
 * @description：
 */
@Mapper
public interface PaymentDao {
    int create(Payment payment);

    Payment getPaymentById(@Param("id") Long id);
}
