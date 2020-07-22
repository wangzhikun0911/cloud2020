package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.domain.CommonResult;
import com.cuslink.springcloud.service.StorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:18
 * @description
 */
@RestController
public class StorageController {

    @Resource
    private StorageService storageService;

    @PostMapping("/storage/decrease")
    public CommonResult decrease(Long productId, Integer count) {
        storageService.decrease(productId, count);
        return new CommonResult(200, "减库存成功");
    }
}
