package com.ucombuy.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by yaosheng on 2020/1/14.
 * 优惠券Kafka消息对象定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponKafkaMessage {

    private Integer status;
    //Coupon主键
    private List<Integer> ids;
}