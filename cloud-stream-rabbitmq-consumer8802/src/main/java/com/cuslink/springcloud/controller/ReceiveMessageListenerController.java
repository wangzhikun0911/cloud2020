package com.cuslink.springcloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


/**
 * @author wangzhikun
 * @date 2020/7/8 0008 23:11
 * @description
 */
@EnableBinding(Sink.class)
@Component
public class ReceiveMessageListenerController {

    @Value("${server.port}")
    private String serverPort;

    @StreamListener(Sink.INPUT)
    public void input(Message<String> message) {
        System.out.println("消费者1号，接受：" + message.getPayload() + "\t port:" + serverPort);
    }
}
