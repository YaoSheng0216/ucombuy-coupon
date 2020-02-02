package com.ucombuy.coupon.dao;

import com.ucombuy.coupon.constant.CouponStatus;
import com.ucombuy.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by yaosheng on 2020/1/7.
 * Coupon Dao接口定义
 */
public interface CouponDao extends JpaRepository<Coupon,Integer> {

    /**
     * 根据 userId + 状态寻找优惠券记录
     * where userId = ... and status = ...
     */
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}