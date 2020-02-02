package com.ucombuy.coupon.vo;

import com.ucombuy.coupon.constant.CouponStatus;
import com.ucombuy.coupon.constant.PeriodType;
import com.ucombuy.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yaosheng on 2020/1/20.
 * 用户优惠券分类，根据优惠券状态分类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponClassify {

    //可以使用的优惠券
    private List<Coupon> usable;

    //已使用的优惠券
    private List<Coupon> used;

    //已过期的优惠券
    private List<Coupon> expired;

    //对当前的优惠券进行分类
    public static CouponClassify classify(List<Coupon> coupons){

        List<Coupon> usable = new ArrayList<> (coupons.size ());
        List<Coupon> used = new ArrayList<> (coupons.size ());
        List<Coupon> expired = new ArrayList<> (coupons.size ());

        coupons.forEach (c -> {

            //判断优惠券是否过期
            boolean isTimeExpire;
            long curTime = new Date().getTime ();

            if(c.getTemplateSDK ().getRule ().getException ().getPeriod ().
                    equals (PeriodType.REGULAR.getCode ())){
                isTimeExpire = c.getTemplateSDK ().getRule ().getException ().getDeadline () <= curTime;
            }else{
                isTimeExpire = DateUtils.addDays (c.getAssignTime (),c.getTemplateSDK ().
                        getRule ().getException ().getGap ()).getTime () <= curTime;
            }

            if(c.getStatus () == CouponStatus.USED){
                used.add (c);
            }else if(c.getStatus () == CouponStatus.EXPIRED || isTimeExpire){
                expired.add (c);
            }else{
                usable.add (c);
            }
        });
        return new CouponClassify (usable,used,expired);
    }
}