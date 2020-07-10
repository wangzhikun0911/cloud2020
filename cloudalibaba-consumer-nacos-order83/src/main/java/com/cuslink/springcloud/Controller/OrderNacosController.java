package com.cuslink.springcloud.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/7/10 0010 23:45
 * @description
 */
@RestController
public class OrderNacosController {

    @Resource
    private RestTemplate restTemplate;

    @Value("${service-url.nacos-user-service}")
    private String serverURI;

    @GetMapping("/consumer/payment/nacos/{id}")
    public String get(@PathVariable("id") String id) {
        return restTemplate.getForObject(serverURI + "/payment/nacos/" + id, String.class);
    }

}
