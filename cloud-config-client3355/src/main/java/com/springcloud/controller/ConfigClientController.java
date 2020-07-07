package com.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangzhikun
 * @date 2020/7/5 0005 11:21
 * @description
 *
 */
@RestController
@RefreshScope //具备刷新能力
public class ConfigClientController {

    @Value("${config.info}")
    private String configInfo;

    @GetMapping("configInfo")
    public String getConfigInfo(){
        return configInfo;
    }
}
