package com.ucombuy.coupon.service;

import com.ucombuy.coupon.entity.CouponTemplate;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.vo.CouponTemplateSDK;


import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yaosheng on 2019/12/31.
 * 优惠券模版基础服务定义
 */
public interface ITemplateBaseService {

    /**
     * 根据优惠券模版 id 获取优惠券模版信息
     * @param id 模版 id
     * @return {link CouponTemplate} 优惠券模版实体
     * @throws CouponException
     */
    CouponTemplate buildTemplateInfo(Integer id) throws CouponException;

    /**
     * 查找所有可用的优惠券模版
     * @return {link CouponTemplateSDK}
     */
    List<CouponTemplateSDK> findAllUsableTemplate();

    /**
     * 获取模版 ids 到 CouponTemplateSDK的映射
     * @param ids 模版 ids
     * @return Map<key : 模版 id,value : CouponTemplateSDK
     */
    Map<Integer,CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}