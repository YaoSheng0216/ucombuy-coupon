package com.ucombuy.coupon.executor;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.vo.GoodsInfo;
import com.ucombuy.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yaosheng on 2020/1/28.
 * 规则执行器抽象类，
 */
public class AbstractExecutor {

    /**
     * 校验商品类型与优惠券是否匹配
     * 注意：
     * 1.这里实现的单品类优惠券校验，多品类优惠券重载此方法
     * 2.商品只需要有一个优惠券要求的商品类型去匹配
     */
    @SuppressWarnings ("all")
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {

        List<Integer> goodsType = settlement.getGoodsInfos ().stream ().map (GoodsInfo::getType).
                collect (Collectors.toList ());

        List<Integer> templateGoodsType = JSON.parseObject (settlement.getCouponAndTemplateInfos ().
                get (0).getTemplateSDK ().getRule ().getUsage ().getGoodsType (), List.class);

        //存在交集即可
        return CollectionUtils.isNotEmpty (CollectionUtils.intersection (goodsType,templateGoodsType));
    }

    /**
     * 处理商品类型与优惠券限制不匹配情况
     * @param settlement {@link SettlementInfo} 用户传递的结算信息
     * @param goodsSum 商品总价
     * @return {@link SettlementInfo} 已经修改过的结算信息
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(SettlementInfo settlement,double goodsSum){

        boolean isGoodsTypeSatisfy = isGoodsTypeSatisfy (settlement);
        //当商品类型不满足时，直接返回总价，并清空优惠券
        if(!isGoodsTypeSatisfy){

            settlement.setCost (goodsSum);
            settlement.setCouponAndTemplateInfos (Collections.emptyList ());

            return settlement;
        }
        return null;
    }

    //商品总价
    protected double goodsCostSum(List<GoodsInfo> goodsInfos){

        return goodsInfos.stream ().mapToDouble (g ->g.getPrice () * g.getCount ()).sum ();
    }

    //保留两位小数
    protected double retain2Decimals(double value){

        return new BigDecimal (value).setScale (2,BigDecimal.ROUND_HALF_UP).doubleValue ();
    }

    //最小支付费用
    protected double minCost(){

        return 0.1;
    }
}