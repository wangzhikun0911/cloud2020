package com.cuslink.springcloud.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author：wangzhikun
 * @date： 2020/6/24 14:08
 * @description：支付类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment implements Serializable{
    private Long id;
    private String serial;

}
