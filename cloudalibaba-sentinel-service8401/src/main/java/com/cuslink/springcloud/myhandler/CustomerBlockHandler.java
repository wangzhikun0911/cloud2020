package com.cuslink.springcloud.myhandler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.cuslink.springcloud.entities.CommonResult;

/**
 * @author wangzhikun
 * @date 2020/7/17 0017 22:56
 * @description
 */
public class CustomerBlockHandler {

    public static CommonResult handleException(BlockException exception){
        return new CommonResult(2020, "自定义限流处理信息....CustomerBlockHandler");
    }
}
