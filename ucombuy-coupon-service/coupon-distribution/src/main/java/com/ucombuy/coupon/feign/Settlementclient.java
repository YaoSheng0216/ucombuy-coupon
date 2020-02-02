package com.ucombuy.coupon.feign;

import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.feign.hystrix.SettlementClientHystrix;
import com.ucombuy.coupon.vo.CommonResponse;
import com.ucombuy.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by yaosheng on 2020/1/20.
 * 优惠券结算微服务Feign接口定义
 */
@FeignClient(value = "eureka-client-coupon-settlement",fallback = SettlementClientHystrix.class)
public interface Settlementclient {

    //优惠券规则计算
    @RequestMapping(value = "/coupon-settlement/settlement/compute",method = RequestMethod.POST)
    CommonResponse<SettlementInfo> computeRule(@RequestBody SettlementInfo settlementInfo) throws CouponException;
}