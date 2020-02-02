package com.ucombuy.coupon.feign.hystrix;

import com.ucombuy.coupon.feign.TemplateClient;
import com.ucombuy.coupon.vo.CommonResponse;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by yaosheng on 2020/1/20.
 * 优惠券模版Feign接口的熔断降级策略
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {

    //查找所有可用的优惠券模版
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {

        log.error ("[eureka-client-coupon-template] findAllUsableTemplate" + "request error ");
        return new CommonResponse<> (-1,"[eureka-client-coupon-template] request error",
                Collections.emptyList ());
    }

    //获取模版ids到CouponTemplateSDK映射
    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {

        log.error ("[eureka-client-coupon-template] findIds2TemplateSDK" + "request error");
        return new CommonResponse<>(-1, "[eureka-client-coupon-template] request error",
                new HashMap<> ()
        );
    }
}