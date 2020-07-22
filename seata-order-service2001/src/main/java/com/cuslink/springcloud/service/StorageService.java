package com.cuslink.springcloud.service;

import com.cuslink.springcloud.domain.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 19:34
 * @description
 */
@FeignClient(value = "seata-storage-service")
public interface StorageService {
    /**
     * 减库存
     */
    @PostMapping("/storage/decrease")
    CommonResult decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
}
