package com.cuslink.springcloud.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 19:54
 * @description
 */
@Configuration
@MapperScan({"com.cuslink.springcloud.dao"})
public class MyBatisConfig {

}
