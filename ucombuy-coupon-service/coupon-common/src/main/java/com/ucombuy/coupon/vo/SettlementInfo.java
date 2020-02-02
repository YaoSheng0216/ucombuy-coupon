package com.ucombuy.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by yaosheng on 2020/1/11.
 * 结算信息对象定义:
 * 1.userId
 * 2.商品信息（列表）
 * 3.优惠券列表
 * 4.结算结果金额
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementInfo {

    //用户id
    private Long userId;

    //商品信息
    private List<GoodsInfo> goodsInfos;

    //优惠券列表
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;

    //是否使结算（核销）生效
    private Boolean employ;

    //结果结算金额
    private Double cost;

    //优惠券与模版信息
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponAndTemplateInfo{

        //Coupon主键
        private Integer id;
        //优惠券对应的模版对象
        private CouponTemplateSDK templateSDK;
    }
}