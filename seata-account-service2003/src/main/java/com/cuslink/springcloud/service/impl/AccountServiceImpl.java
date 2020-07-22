package com.cuslink.springcloud.service.impl;

import com.cuslink.springcloud.dao.AccountDao;
import com.cuslink.springcloud.service.AccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:34
 * @description
 */
@Service
public class AccountServiceImpl implements AccountService{

    @Resource
    private AccountDao accountDao;
    @Override
    public void decrease(Long userId, BigDecimal money) {
        accountDao.decrease(userId,money);
    }
}
