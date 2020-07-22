package com.cuslink.springcloud.service;

import com.cuslink.springcloud.domain.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 19:42
 * @description
 */
@FeignClient(value = "seata-account-service")
public interface AccountService {
    /**
     * 减账户
     */
    @PostMapping("/account/decrease")
    CommonResult decrease(@RequestParam("userId") Long userId, @RequestParam("money") BigDecimal money);
}
