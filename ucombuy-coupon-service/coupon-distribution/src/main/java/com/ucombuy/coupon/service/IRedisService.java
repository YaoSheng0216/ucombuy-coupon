package com.ucombuy.coupon.service;

import com.ucombuy.coupon.entity.Coupon;
import com.ucombuy.coupon.exception.CouponException;

import java.util.List;

/**
 * Created by yaosheng on 2020/1/7.
 * Redis 相关的操作服务接口定义
 * 1.用户的三个状态优惠券 Cache 相关操作
 * 2.优惠券模版生成的优惠券码 Cache 操作
 */
public interface IRedisService {

    //根据userId和状态找到缓存的优惠券列表数据，接口有可能会返回null，代表没有记录
    List<Coupon> getCacheCoupons(Long userId,Integer status);

    //保存空的优惠券列表到缓存中
    void saveEmptyCouponListToCache(Long userId,List<Integer> status);

    //尝试从cache中获取一个优惠券码
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    //将优惠券保存到cache中
    Integer addCouponToCache(Long usrId,List<Coupon> coupons,Integer status)throws CouponException;
}