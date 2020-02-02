package com.ucombuy.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yaosheng on 2019/12/31.
 * 微服务之间的优惠券模版信息定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplateSDK {

    private Integer id;
    private String name;
    private String logo;
    private String desc;
    private String category;
    private Integer productLine;
    private String key;
    private Integer target;
    private TemplateRule rule;
}
