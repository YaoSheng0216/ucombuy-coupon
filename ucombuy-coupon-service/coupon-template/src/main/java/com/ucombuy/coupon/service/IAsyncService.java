package com.ucombuy.coupon.service;

import com.ucombuy.coupon.entity.CouponTemplate;

/**
 * Created by yaosheng on 2019/12/31.
 * 异步服务接口定义
 */
public interface IAsyncService {

    //根据模版异步的创建优惠券码
    void asyncConstructCouponByTemplate(CouponTemplate template);
}