package com.ucombuy.coupon.executor;

import com.ucombuy.coupon.constant.CouponCategory;
import com.ucombuy.coupon.constant.RuleFlag;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yaosheng on 2020/1/28.
 * 优惠券结算规则执行管理器
 * 即根据用户请求（SettlementInfos）找到对应的Executor，去做结算
 * BeanPostProcessor:Bean后置处理器
 */
@Slf4j
@Component
@SuppressWarnings ("all")
public class ExecutorManager implements BeanPostProcessor {

    private static Map<RuleFlag,RuleExecutor> execitorIndex = new HashMap<> (RuleFlag.values ().length);

    /**
     * 优惠券规则计算入口
     * 注意:一定要保证传递进来的优惠券个数 >= 1
     */
    public SettlementInfo computeRule(SettlementInfo settlement) throws CouponException{

        SettlementInfo result = null;

        //单类优惠券
        if(settlement.getCouponAndTemplateInfos ().size () == 1){

            //获取优惠券规则
            CouponCategory category = CouponCategory.of (settlement.getCouponAndTemplateInfos ().
                    get (0).getTemplateSDK ().getCategory ());
            switch (category){
                case MANJIAN:
                    result = execitorIndex.get (RuleFlag.MANJIAN).computeRule (settlement);
                    break;
                case ZHEKOU:
                    result = execitorIndex.get (RuleFlag.ZHEKOU).computeRule (settlement);
                    break;
                case LIJIAN:
                    result = execitorIndex.get (RuleFlag.LIJIAN).computeRule (settlement);
                    break;
            }
        }else{
            //多类优惠券
            List<CouponCategory> categories = new ArrayList<> (settlement.getCouponAndTemplateInfos ().size ());
            settlement.getCouponAndTemplateInfos ().forEach (ct -> categories.add
                    (CouponCategory.of (ct.getTemplateSDK ().getCategory ())));
            if(categories.size () != 2){
                throw new CouponException ("Not Support For More" + "Template Category");
            }else{
                if(categories.contains (CouponCategory.MANJIAN) && categories.contains (CouponCategory.ZHEKOU)){
                    result = execitorIndex.get (RuleFlag.MANJIAN_ZHEKOU).computeRule (settlement);
                }else{
                    throw new CouponException ("Not Support For Other" + "Template Category");
                }
            }
        }
        return result;
    }

    //在 bean 初始化之前去执行（before)
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        if(!(bean instanceof RuleExecutor)){
            return bean;
        }

        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig ();

        if(execitorIndex.containsKey (ruleFlag)){
            throw new IllegalArgumentException ("There is already an executor" + "for rule flag" + ruleFlag);
        }

        log.info ("Load executor {} for rule flag {}",executor.getClass (),ruleFlag );
        execitorIndex.put (ruleFlag,executor);

        return null;
    }

    //在 bean 初始化之后去执行（after）
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}