package com.ucombuy.coupon.converter;

import com.ucombuy.coupon.constant.CouponStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by yaosheng on 2020/1/7.
 * 优惠券状态枚举属性转换器
 */
@Converter
public class CouponStatusConverter implements AttributeConverter<CouponStatus,Integer> {

    @Override
    public Integer convertToDatabaseColumn(CouponStatus status) {
        return status.getCode ();
    }

    @Override
    public CouponStatus convertToEntityAttribute(Integer code) {
        return CouponStatus.of (code);
    }
}