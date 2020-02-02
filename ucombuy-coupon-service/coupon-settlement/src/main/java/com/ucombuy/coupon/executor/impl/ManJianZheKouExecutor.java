package com.ucombuy.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.constant.CouponCategory;
import com.ucombuy.coupon.constant.RuleFlag;
import com.ucombuy.coupon.executor.AbstractExecutor;
import com.ucombuy.coupon.executor.RuleExecutor;
import com.ucombuy.coupon.vo.GoodsInfo;
import com.ucombuy.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yaosheng on 2020/1/28.
 * 满减 + 折扣优惠券结算规则执行器
 */
@Slf4j
@Component
@SuppressWarnings ("all")
public class ManJianZheKouExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * 规则类型标记
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    /**
     * 检验商品类型与优惠券是否匹配
     * 注意:
     * 1.这楼实现单品类优惠券的校验，多品类优惠券重载此方法
     * 2.如果想要使用多类优惠券，则必须要所有的商品类型都包含在内，即差集为空
     * @param settlement {@link SettlementInfo} 用户传递的计算信息
     */
    @Override
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {

        log.debug ("Check ManJian And ZheKou Is Match Or Not");
        List<Integer> goodsType = settlement.getGoodsInfos ().stream ().map (GoodsInfo::getType).
                collect (Collectors.toList ());
        List<Integer> templateGoodsType = new ArrayList<> ();
        settlement.getCouponAndTemplateInfos ().forEach (ct -> {templateGoodsType.addAll
                (JSON.parseObject (ct.getTemplateSDK ().getRule ().getUsage ().getGoodsType (),List.class));
        });

        //如果想要使用多类优惠券，则必须要所有的商品类型都包含在内，即差集为空

        return CollectionUtils.isEmpty (CollectionUtils.subtract (goodsType,templateGoodsType));
    }

    /**
     * 优惠券规则计算
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {

        double goodsSum = retain2Decimals (goodsCostSum (settlement.getGoodsInfos ()));
        SettlementInfo probability = processGoodsTypeNotSatisfy (settlement,goodsSum);
        if(null != probability){

            log.debug ("ManJian And ZheKou Template Is Not Match To GoodsType");
            return probability;
        }

        SettlementInfo.CouponAndTemplateInfo manjian = null;
        SettlementInfo.CouponAndTemplateInfo zhekou = null;
        for(SettlementInfo.CouponAndTemplateInfo ct : settlement.getCouponAndTemplateInfos ()){

            if(CouponCategory.of (ct.getTemplateSDK ().getCategory ()) == CouponCategory.MANJIAN){
                manjian = ct;
            }else{
                zhekou = ct;
            }
        }

        //保证满减和折扣一定是不为空的，一定可以从方法里面获取到满减和折扣两张优惠券
        assert null != manjian;
        assert null != zhekou;

        //当前的优惠券和满减券如果不能同时使用（一起使用），清空优惠券，返回商品原价
        if(!isTemplateCanShared (manjian,zhekou)){

            log.debug ("Current ManJian And ZheKou Can Not Shared");
            settlement.setCost (goodsSum);
            settlement.setCouponAndTemplateInfos (Collections.emptyList ());
            return settlement;
        }

        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<> ();
        double manJianBase = (double) manjian.getTemplateSDK ().getRule ().getDiscount ().getBase ();
        double manJianQuota = (double) manjian.getTemplateSDK ().getRule ().getDiscount ().getQuota ();

        //最终的价格
        double targetSum = goodsSum;
        if(targetSum >= manJianBase){
            targetSum -= manJianQuota;
            ctInfos.add (manjian);
        }

        //计算折扣
        double zheKouQuota = (double)zhekou.getTemplateSDK ().getRule ().getDiscount ().getQuota ();
        targetSum *= zheKouQuota * 1.0 / 100;
        ctInfos.add (zhekou);

        settlement.setCouponAndTemplateInfos (ctInfos);
        settlement.setCost (retain2Decimals (targetSum > minCost () ? targetSum : minCost ()));

        log.debug ("Use ManJian And ZheKou Coupon Make Goods Cost From {} To {}",goodsSum,settlement.getCost ());

        return settlement;
    }

    //当前两张优惠券是否可以共用，即校验TemplateRule中的weight是否满足条件
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manjian,SettlementInfo.
                                        CouponAndTemplateInfo zhekou){
        String manjianKey = manjian.getTemplateSDK ().getKey () + String.format
                ("%04d",manjian.getTemplateSDK ().getId ());
        String zhekouKey = zhekou.getTemplateSDK ().getKey () + String.format ("%04d",zhekou.getTemplateSDK ().getId ());
        List<String> allSharedKeysForManjian = new ArrayList<> ();
        allSharedKeysForManjian.add (manjianKey);
        allSharedKeysForManjian.addAll (JSON.parseObject (manjian.getTemplateSDK ().getRule ().getWeight (),List.class));

        List<String> allSharedKeysForZhekou = new ArrayList<> ();
        allSharedKeysForZhekou.add (zhekouKey);
        allSharedKeysForZhekou.addAll (JSON.parseObject (zhekou.getTemplateSDK ().getRule ().getWeight (),List.class));

        return CollectionUtils.isSubCollection (Arrays.asList (manjianKey,zhekouKey),allSharedKeysForManjian) ||
                CollectionUtils.isSubCollection (Arrays.asList (manjianKey,zhekouKey),allSharedKeysForZhekou);
    }
}