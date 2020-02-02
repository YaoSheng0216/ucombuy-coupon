package com.ucombuy.coupon.service.impl;

import com.ucombuy.coupon.dao.CouponTemplateDao;
import com.ucombuy.coupon.entity.CouponTemplate;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.service.ITemplateBaseService;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by yaosheng on 2020/1/1.
 * 优惠券模版基础服务接口实现
 * stream()为Java 8新提供的编程方式
 */
@Slf4j
@Service
public class TemplateServiceImpl implements ITemplateBaseService {

    @Autowired
    private CouponTemplateDao templateDao;

    /**
     * 根据优惠券模版id获取优惠券模版信息
     * @param id 模版 id
     * @return {@link CouponTemplate} 优惠券模版实体
     * @throws CouponException
     */
    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {

        Optional<CouponTemplate> template = templateDao.findById(id);
        if(!template.isPresent ()){
            throw new CouponException ("Template Is Not Exist:" + id);
        }
        return template.get ();
    }

    /**
     * 查找所有可用的优惠券模版
     * @return {@link CouponTemplateSDK}
     */
    @Override
    public List<CouponTemplateSDK> findAllUsableTemplate() {

        List<CouponTemplate> templates = templateDao.findAllByAvailableAndExpired (
                true,false);
        return templates.stream ().map (this::template2TemplateSDK).collect (Collectors.toList ());
    }

    /**
     * 获取模版 ids 到 CouponTemplateSDK的映射
     * @param ids 模版 ids
     * @return Map<key: 模版 id, value: CouponTemplateSDK>
     */
    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {

        List<CouponTemplate> templates = templateDao.findAllById (ids);
        return templates.stream ().map (this::template2TemplateSDK).collect (Collectors.toMap (
                CouponTemplateSDK::getId, Function.identity ()
        ));
    }

    /**
     * 将CouponTemplate转换为CouponTemplateSDK
     */
    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template){

        return new CouponTemplateSDK (
                template.getId (),
                template.getName (),
                template.getLogo (),
                template.getDesc (),
                template.getCategory ().getCode (),
                template.getProductLine ().getCode (),
                template.getKey (),              //并非是拼装好的Template Key
                template.getTarget ().getCode (),
                template.getRule ()
                );
    }
}