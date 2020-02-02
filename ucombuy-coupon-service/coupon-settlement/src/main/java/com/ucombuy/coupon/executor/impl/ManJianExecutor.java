package com.ucombuy.coupon.executor.impl;

import com.ucombuy.coupon.constant.RuleFlag;
import com.ucombuy.coupon.executor.AbstractExecutor;
import com.ucombuy.coupon.executor.RuleExecutor;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import com.ucombuy.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Created by yaosheng on 2020/1/28.
 *  满减优惠券结算规则执行器
 */
@Slf4j
@Component
@SuppressWarnings ("all")
public class ManJianExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 规则类型标记
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN;
    }

    /**
     *优惠券规则计算
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {

        double goodsSum = retain2Decimals(goodsCostSum (settlement.getGoodsInfos ()));
        SettlementInfo probability = processGoodsTypeNotSatisfy (settlement,goodsSum);
        if( null != probability){
            log.debug ("ManJian Template Is Not Match To GoodsType");
            return probability;
        }

        //判断满减是否满足折扣标准
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos ().get (0).getTemplateSDK ();
        double base = (double)templateSDK.getRule ().getDiscount ().getBase ();
        double quota = (double)templateSDK.getRule ().getDiscount ().getQuota ();

        //如果不符合标准，则直接返回商品总价:结算
        if(goodsSum < base){

            log.debug ("Current Goods Cost Sum < ManJian Coupon Base");
            settlement.setCost (goodsSum);
            settlement.setCouponAndTemplateInfos (Collections.emptyList ());
            return settlement;
        }

        //计算使用优惠券之后的价格
        settlement.setCost (retain2Decimals ((goodsSum - quota) > minCost () ? (goodsSum - quota) : minCost ()));
        log.debug ("Use ManJian Coupon Make Goods Cost From {} To {}",goodsSum,settlement.getCost ());

        return settlement;
    }
}