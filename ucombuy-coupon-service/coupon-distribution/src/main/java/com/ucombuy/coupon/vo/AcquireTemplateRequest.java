package com.ucombuy.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yaosheng on 2020/1/11.
 * 获取优惠券请求对象定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcquireTemplateRequest {

    private Long userId;
    //优惠券模版信息
    private CouponTemplateSDK templateSDK;
}