package com.ucombuy.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.entity.Coupon;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.service.IUserService;
import com.ucombuy.coupon.vo.AcquireTemplateRequest;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import com.ucombuy.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by yaosheng on 2020/1/22.
 * 用户服务Controller
 */
@Slf4j
@RestController
public class UserServiceController {

    @Autowired
    private IUserService iUserService;

    //根据用户id和优惠券状态查找用户优惠券记录
    @GetMapping("/coupons")
    public List<Coupon> findCouponsByStatus(@RequestParam("userId") Long userId,
                                            @RequestParam("status") Integer status) throws CouponException{

        log.info ("Find Coupons By Status : {},{}",userId,status);
        return iUserService.findCouponsByStatus (userId, status);
    }

    //根据用户id查找当前可以领取的优惠券模版
    @GetMapping("/template")
    public List<CouponTemplateSDK> findAvailableTemplate(@RequestParam("userId") Long userId) throws CouponException{

        log.info ("Find Available Template : {}",userId);
        return iUserService.findAvailableTemplate (userId);
    }

    //用户领取优惠券
    @PostMapping("/acquire/template")
    public Coupon acquireTemplate(@RequestBody AcquireTemplateRequest request) throws CouponException{

        log.info ("Acquire Template : {}", JSON.toJSONString (request));
        return iUserService.acquireTemplate (request);
    }

    //结算（核销）优惠券
    @PostMapping("/settlement")
    public SettlementInfo settlement(SettlementInfo info) throws CouponException{

        log.info ("Settlement L {}",JSON.toJSONString (info));
        return iUserService.settlement (info);
    }
}