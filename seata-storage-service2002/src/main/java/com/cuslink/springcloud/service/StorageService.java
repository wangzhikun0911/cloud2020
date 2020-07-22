package com.cuslink.springcloud.service;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:16
 * @description
 */
public interface StorageService {
    void decrease(Long productId, Integer count);
}
