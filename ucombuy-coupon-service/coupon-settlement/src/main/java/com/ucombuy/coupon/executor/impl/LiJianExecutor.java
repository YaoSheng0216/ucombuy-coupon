package com.ucombuy.coupon.executor.impl;

import com.ucombuy.coupon.constant.RuleFlag;
import com.ucombuy.coupon.executor.AbstractExecutor;
import com.ucombuy.coupon.executor.RuleExecutor;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import com.ucombuy.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by yaosheng on 2020/1/28.
 * 立减优惠券结算规则执行器
 */
@Slf4j
@Component
public class LiJianExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 规则类型标记
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.LIJIAN;
    }

    /**
     * 优惠券规则计算
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return @{@link SettlementInfo} 修正过的结算信息
     */
    @Override
    @SuppressWarnings ("all")
    public SettlementInfo computeRule(SettlementInfo settlement) {

        double goodsSum = retain2Decimals (goodsCostSum (settlement.getGoodsInfos ()));
        SettlementInfo probability = processGoodsTypeNotSatisfy (settlement,goodsSum);
        if(null != probability){
            log.debug ("LiJian Template Is Not Match To GoodsType");
            return probability;
        }

        //立减优惠券直接使用，没有门槛
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos ().get (0).getTemplateSDK ();
        double quota = (double)templateSDK.getRule ().getDiscount ().getQuota ();

        //计算使用优惠券之后的价格:结算
        settlement.setCost (retain2Decimals (goodsSum - quota) > minCost () ?
                retain2Decimals (goodsSum - quota) : minCost ());
        log.debug ("Use LiJian Coupon Make Goods Cost From {} To {}",goodsSum,settlement.getCost ());

        return settlement;
    }
}