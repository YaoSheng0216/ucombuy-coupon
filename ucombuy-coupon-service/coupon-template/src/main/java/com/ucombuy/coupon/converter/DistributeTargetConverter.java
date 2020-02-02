package com.ucombuy.coupon.converter;

import com.ucombuy.coupon.constant.DistributeTarget;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by yaosheng on 2019/12/30.
 */
@Converter
public class DistributeTargetConverter implements AttributeConverter<DistributeTarget,Integer> {


    @Override
    public Integer convertToDatabaseColumn(DistributeTarget distributeTarget) {
        return distributeTarget.getCode ();
    }

    @Override
    public DistributeTarget convertToEntityAttribute(Integer code) {
        return DistributeTarget.of (code);
    }
}