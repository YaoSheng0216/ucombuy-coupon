package com.ucombuy.coupon.vo;

import com.ucombuy.coupon.constant.CouponCategory;
import com.ucombuy.coupon.constant.DistributeTarget;
import com.ucombuy.coupon.constant.ProductLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by yaosheng on 2019/12/31.
 * 优惠券模版创建请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRequest {

    private String name;
    private String logo;
    private String desc;
    private String category;
    private Integer productLine;            //产品线
    private Integer count;                  //总数
    private Long userId;                    //创建用户
    private Integer target;                 //目标用户
    private TemplateRule rule;              //优惠券规则

    //校验对象的合法性
    public boolean volidate(){

        boolean stringValid = StringUtils.isNoneEmpty (name) && StringUtils.isNotEmpty (logo)
                && StringUtils.isNotEmpty (desc);
        boolean enumValid = null != CouponCategory.of (category) && null != ProductLine.of (productLine)
                && null != DistributeTarget.of (target);
        boolean numValid = count > 0 && userId >0;

        return stringValid && enumValid && numValid && rule.validate ();
    }
}