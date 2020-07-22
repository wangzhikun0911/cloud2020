package com.cuslink.springcloud.service;

import com.cuslink.springcloud.entities.CommonResult;
import com.cuslink.springcloud.entities.Payment;
import org.springframework.stereotype.Component;

/**
 * @author wangzhikun
 * @date 2020/7/18 0018 23:55
 * @description
 */
@Component
public class PaymentFallbackService implements PaymentService {
    @Override
    public CommonResult<Payment> paymentSQL(Long id)
    {
        return new CommonResult<>(44444,"服务降级返回,---PaymentFallbackService",new Payment(id,"errorSerial"));
    }

}
