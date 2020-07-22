package com.cuslink.springcloud.service.impl;

import com.cuslink.springcloud.dao.StorageDao;
import com.cuslink.springcloud.service.StorageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wangzhikun
 * @date 2020/7/19 0019 20:17
 * @description
 */
@Service
public class StorageServiceImpl implements StorageService {
    @Resource
    private StorageDao storageDao;

    @Override
    public void decrease(Long productId, Integer count) {
        storageDao.decrease(productId,count);
    }
}
