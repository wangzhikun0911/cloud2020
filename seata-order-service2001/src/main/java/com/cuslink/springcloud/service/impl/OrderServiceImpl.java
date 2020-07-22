package com.cuslink.springcloud.service.impl;

import com.cuslink.springcloud.dao.OrderDao;
import com.cuslink.springcloud.domain.Order;
import com.cuslink.springcloud.service.AccountService;
import com.cuslink.springcloud.service.OrderService;
import com.cuslink.springcloud.service.StorageService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
        * @author wangzhikun
        * @date 2020/7/19 0019 19:29
        * @description
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Resource
    private StorageService storageService;

    @Resource
    private AccountService accountService;

    @Resource
    private OrderDao orderDao;

    @Override
    @GlobalTransactional(name="fsp-create-order",rollbackFor = Exception.class)
    public void createOrder(Order order) {
        log.info("开始下订单");
        //1.下订单
        orderDao.createOrder(order);
        log.info("下订单成功，开始减库存");
        //2.减库存，
        storageService.decrease(order.getProductId(), order.getCount());
        log.info("减库存成功，开始减账户");
        //3.减账户
        accountService.decrease(order.getUserId(),order.getMoney());
        log.info("减账户成功,开始修改订单状态");
        //4.修改订单状态
        orderDao.updateStatus(order.getUserId(),0);
        log.info("下单成功");
    }

}
