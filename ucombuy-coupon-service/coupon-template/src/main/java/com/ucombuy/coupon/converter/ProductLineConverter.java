package com.ucombuy.coupon.converter;

import com.ucombuy.coupon.constant.ProductLine;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by yaosheng on 2019/12/30.
 * 产品线枚举属性转换器
 */
@Converter
public class ProductLineConverter implements AttributeConverter<ProductLine,Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProductLine productLine) {
        return productLine.getCode ();
    }

    @Override
    public ProductLine convertToEntityAttribute(Integer code) {
        return ProductLine.of (code);
    }
}