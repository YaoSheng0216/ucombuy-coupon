package com.ucombuy.coupon.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ucombuy.coupon.constant.CouponStatus;
import com.ucombuy.coupon.converter.CouponStatusConverter;
import com.ucombuy.coupon.serialization.CouponSerialize;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by yaosheng on 2020/1/7.
 * 优惠券（用户领取的优惠券记录）实体表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners (AuditingEntityListener.class)
@Table(name = "coupon")
@JsonSerialize(using = CouponSerialize.class)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Integer id;

    @Column(name = "template_id",nullable = false)
    private Integer templateId;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name ="coupon_code",nullable = false)
    private String couponCode;

    //领取时间
    //@CreatedData     Jpa审计功能
    @Column(name = "assign_time",nullable = false)
    private Date assignTime;

    //优惠券状态
    @Basic
    @Column(name = "status",nullable = false)
    @Convert(converter = CouponStatusConverter.class)
    private CouponStatus status;

    @Transient
    private CouponTemplateSDK templateSDK;

    //返回一个无效的Coupon对象
    public static Coupon invalidCoupon(){
        Coupon coupon = new Coupon ();
        coupon.setId (-1);
        return coupon;
    }

    //构造优惠券
    public Coupon(Integer templateId,Long userId,String couponCode,CouponStatus status){

        this.templateId = templateId;
        this.userId = userId;
        this.couponCode = couponCode;
        this.status = status;
    }
}