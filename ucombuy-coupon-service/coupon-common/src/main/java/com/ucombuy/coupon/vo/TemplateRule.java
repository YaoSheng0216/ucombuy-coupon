package com.ucombuy.coupon.vo;

import com.ucombuy.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by yaosheng on 2019/12/30.
 * 优惠劵规则对象定义
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRule {

    //优惠券过期规则
    private Exception exception;
    //折扣
    private Discount discount;
    //每个人最多可以领取几张优惠券的限制
    private Integer limitation;
    //使用范围的限制
    private Usage usage;
    //权重(可以与那些优惠券叠加使用，同一类优惠券不可以叠加）:list[](优惠券的唯一编码)
    private String weight;

    //校验功能
    public boolean validate(){

        return exception.validate() && discount.validate ()
                && limitation > 0 && usage.validate ()
                && StringUtils.isNotEmpty (weight);
    }


    //有效期规则
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Exception{
        //有效期规则，对应PeriodType的code字段
        private Integer period;
        //有效间隔，只对变动性有效期有效
        private Integer gap;
        //优惠劵模版的失效日期，两类规则都有效
        private Long deadline;

        boolean validate(){
            //最简化校验
            return null != PeriodType.of (period) && gap > 0 && deadline >0;
        }
    }


    //折扣，需要与类型配合决定
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount{
        //额度，满减（20），折扣（85），立减（10）
        private Integer quota;
        //基准，需要满多少才可用
        private Integer base;
        boolean validate(){
            return quota > 0 && base > 0;
        }
    }


    //使用范围
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage{
        //省份
        private String province;
        //城市
        private String city;
        //商品类型，list[文娱，生鲜，家居]
        private String goodsType;
        boolean validate(){
            return StringUtils.isNotEmpty (province) && StringUtils.isNotEmpty (city)
                    && StringUtils.isNotEmpty (goodsType);
        }
    }
}