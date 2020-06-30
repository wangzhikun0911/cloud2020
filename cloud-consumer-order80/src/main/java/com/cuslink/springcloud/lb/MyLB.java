package com.cuslink.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangzhikun
 * @date 2020/6/30 0030 10:57
 * @description
 */
@Component
public class MyLB implements LoadBalance {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public final int getAndIncreament(){
        int current;
        int next;
        do{
            current = this.atomicInteger.get();
            next = current > Integer.MAX_VALUE ? 0 :current + 1;
        }while(!this.atomicInteger.compareAndSet(current,next));
        System.out.println("********第几次访问，次数next:"+next);
        return next;
    }

    @Override
    public ServiceInstance getServiceInstance(List<ServiceInstance> instanceList) {
        int index = getAndIncreament() % instanceList.size();
        return instanceList.get(index);
    }
}
