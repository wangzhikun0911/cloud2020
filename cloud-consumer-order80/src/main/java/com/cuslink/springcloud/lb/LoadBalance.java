package com.cuslink.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 10:55
 * @description
 */
public interface LoadBalance {

    ServiceInstance getServiceInstance(List<ServiceInstance> instanceList);
}
