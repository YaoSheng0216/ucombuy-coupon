package com.ucombuy.coupon.service;

import com.ucombuy.coupon.entity.CouponTemplate;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.vo.TemplateRequest;

/**
 * Created by yaosheng on 2019/12/31.
 * 构建优惠券模版定义
 */
public interface IBuildTemplateService {

    /**
     * 创建优惠券模版
     * @param request {@link TemplateRequest} 模版信息请求对象
     * @return {@link CouponTemplate} 优惠券模版实体
     * @throws CouponException
     */
    CouponTemplate buildTemplate(TemplateRequest request) throws CouponException;
}
