package com.ucombuy.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.executor.ExecutorManager;
import com.ucombuy.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yaosheng on 2020/1/28.
 * 结算微服务的Controller
 */
@Slf4j
@RestController
public class SettlementController {

    @Autowired
    private ExecutorManager executorManager;

    /**
     * 优惠券结算
     * 127.0.0.1:7003/coupon-settlement/settlement/compute
     * 127.0.0.1:9000/ucombuy/coupon-settlement/settlement/compute
     */
    @PostMapping("/settlement/compute")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlement) throws CouponException{

        log.info ("settlement : {}", JSON.toJSONString (settlement));
        return executorManager.computeRule (settlement);
    }
}