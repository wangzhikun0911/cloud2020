package com.cuslink.springcloud.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:27
 * @description
 */
@Mapper
public interface AccountDao {
    void decrease(@Param("userId")Long userId, @Param("money")BigDecimal money);
}
