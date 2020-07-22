package com.cuslink.springcloud.service;

import java.math.BigDecimal;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:34
 * @description
 */
public interface AccountService {
    void decrease(Long userId, BigDecimal money);
}
