package com.ucombuy.coupon.service;

import com.ucombuy.coupon.entity.Coupon;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.vo.AcquireTemplateRequest;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import com.ucombuy.coupon.vo.SettlementInfo;

import java.util.List;

/**
 * Created by yaosheng on 2020/1/11.
 * 用户相关的接口定义
 * 1.用户的三类状态优惠券信息展示服务
 * 2.查看用户当前可以领取的优惠券模版
 * 3.用户领取优惠券服务
 * 4.用户消费优惠券服务（结合Coupon-settlement模块实现）
 */
public interface IUserService {

    //根据用户id和状态查询优惠券记录
    List<Coupon> findCouponsByStatus(Long userId,Integer status)throws CouponException;

    //根据用户id查看当前可以领取的优惠券模版
    List<CouponTemplateSDK> findAvailableTemplate(Long userId)throws CouponException;

    //用户领取优惠券
    Coupon acquireTemplate(AcquireTemplateRequest request)throws CouponException;

    //结算（核销）优惠券
    SettlementInfo settlement(SettlementInfo info)throws CouponException;
}