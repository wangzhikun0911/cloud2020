package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.service.IMessageProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/7/8 0008 22:58
 * @description
 */
@RestController
public class SendMessageController {
    @Resource
    private IMessageProvider iMessageProvider;

    @GetMapping("/sendMessage")
    public String send() {
        return iMessageProvider.send();
    }
}
