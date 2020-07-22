package com.cuslink.springcloud.controller;

import com.cuslink.springcloud.domain.CommonResult;
import com.cuslink.springcloud.service.AccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:36
 * @description
 */
@RestController
public class AccountController {

    @Resource
    private AccountService accountService;


    @PostMapping("/account/decrease")
    public CommonResult decrease(@RequestParam("userId") Long userId, @RequestParam("money") BigDecimal money){
        accountService.decrease(userId,money);
        return new CommonResult(200,"减账户成功");
    }
}
