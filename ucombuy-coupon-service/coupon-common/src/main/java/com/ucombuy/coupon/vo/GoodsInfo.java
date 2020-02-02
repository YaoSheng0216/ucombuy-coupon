package com.ucombuy.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.omg.CORBA.INTERNAL;

/**
 * Created by yaosheng on 2020/1/11.
 * fake 商品信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsInfo {

    //商品类型
    private Integer type;
    //商品价格
    private Double price;
    //商品数量
    private Integer count;

    // TODO 名称，使用信息
}