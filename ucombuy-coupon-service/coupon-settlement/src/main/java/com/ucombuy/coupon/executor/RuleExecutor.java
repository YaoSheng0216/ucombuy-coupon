package com.ucombuy.coupon.executor;

import com.ucombuy.coupon.constant.RuleFlag;
import com.ucombuy.coupon.vo.SettlementInfo;

/**
 * Created by yaosheng on 2020/1/28.
 * 优惠券模版规则处理器接口定义
 */
public interface RuleExecutor {

    //规则类型标记
    RuleFlag ruleConfig();
    //优惠券规则计算
    SettlementInfo  computeRule(SettlementInfo settlement);
}