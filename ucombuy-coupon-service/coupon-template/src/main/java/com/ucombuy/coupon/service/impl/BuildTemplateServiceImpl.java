package com.ucombuy.coupon.service.impl;

import com.ucombuy.coupon.dao.CouponTemplateDao;
import com.ucombuy.coupon.entity.CouponTemplate;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.service.IAsyncService;
import com.ucombuy.coupon.service.IBuildTemplateService;
import com.ucombuy.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yaosheng on 2020/1/1.
 * 构建优惠券模版接口实现
 */
@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {

    @Autowired
    private IAsyncService asyncService;

    @Autowired
    private CouponTemplateDao templateDao;

    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {

        //参数合法性校验
        if(!request.volidate ()){
            throw new CouponException ("BuildTemplate Param Is Not Valid");
        }
        //判断同名的优惠券模版是否存在
        if(null != templateDao.findByName (request.getName ())){
            throw new CouponException ("Exist Same Name Template");
        }
        //构造CouponTemplate并保存到数据库中
        CouponTemplate template = requestToTemplate (request);
        template = templateDao.save (template);

        //根据优惠券模版异步生成优惠券码
        asyncService.asyncConstructCouponByTemplate (template);
        return template;
    }

    //将TemplateRequest转换为CouponTemplate
    private CouponTemplate requestToTemplate(TemplateRequest  request){
        return new CouponTemplate (
                request.getName (),
                request.getLogo (),
                request.getDesc (),
                request.getCategory (),
                request.getProductLine (),
                request.getCount (),
                request.getUserId (),
                request.getTarget (),
                request.getRule ()
        );
    }
}