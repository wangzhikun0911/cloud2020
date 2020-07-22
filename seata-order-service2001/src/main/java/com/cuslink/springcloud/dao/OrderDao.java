package com.cuslink.springcloud.dao;

import com.cuslink.springcloud.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 19:13
 * @description
 */
@Mapper
public interface OrderDao {
    void createOrder(Order order);

    void updateStatus(@Param("userId")Long id,@Param("status")Integer status);
}
