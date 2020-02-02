package com.ucombuy.coupon.feign.hystrix;

import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.feign.Settlementclient;
import com.ucombuy.coupon.vo.CommonResponse;
import com.ucombuy.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by yaosheng on 2020/1/20.
 * 结算微服务熔断策略实现
 */
@Slf4j
@Component
public class SettlementClientHystrix implements Settlementclient {

    //优惠券规则计算
    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlementInfo) throws CouponException {

        log.error ("[eureka-client-coupon-settlement] computeRule" + "request error");
        settlementInfo.setEmploy (false);
        settlementInfo.setCost (-1.0);
        return new CommonResponse<> (-1,"[eureka-client-coupon-settlement] computeRule" + "request error");
    }
}