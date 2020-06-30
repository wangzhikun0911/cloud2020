package com.cuslink.myrule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangzhikun
 * @date 2020/6/28 0028 12:46
 * @description：自定义一个配置类，不放在@ComponentScan扫描的当前包下及其子包下
 */
@Configuration
public class MyIRule {

    @Bean
    public IRule myRule(){
        return new RandomRule();//随机
    }
}
