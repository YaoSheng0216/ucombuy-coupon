package com.ucombuy.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.entity.CouponTemplate;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.service.IBuildTemplateService;
import com.ucombuy.coupon.service.ITemplateBaseService;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import com.ucombuy.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;;
import java.util.List;
import java.util.Map;

/**
 * Created by yaosheng on 2020/1/1.
 * 优惠劵模板的相关功能控制器
 */
@Slf4j
@RestController
public class CouponTemplateController {

    //构建优惠券模版服务
    @Autowired
    private IBuildTemplateService buildTemplateService;

    //优惠券模版基础服务
    @Autowired
    private ITemplateBaseService templateBaseService;

    //构建优惠券模版访问路径 : localhost:7001/coupon-template/template/build
    //Zuul网关的访问路径 : localhost:9000/ucombuy/coupon-template/template/build
    @PostMapping("/template/build")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request) throws CouponException{

        log.info ("Build Template: {}", JSON.toJSONString (request));
        return buildTemplateService.buildTemplate (request);
    }

    //构造优惠券模版详情访问路径 : localhost:7001/coupon-template/template/info?id=1
    @GetMapping("/template/info")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id) throws CouponException{

        log.info ("Build Template Info For : {}", id);
        return templateBaseService.buildTemplateInfo (id);
    }

    //查找所有可用的优惠券模版访问路径 : localhost:7001/coupon-template/template/sdk/all
    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate(){

        log.info ("Find All Usable Template");
        return templateBaseService.findAllUsableTemplate ();
    }

    //获取模版ids到CouponTemplateSDK的映射访问路径 : localhost:7001/coupon-template/template/sdk/infos
    @GetMapping("/template/sdk/infos")
    public Map<Integer,CouponTemplateSDK> findIds2TemplateSDK(@RequestParam("ids") Collection<Integer> ids){

        log.info ("FindIds2TemplateSDK : {}",JSON.toJSONString (ids));
        return templateBaseService.findIds2TemplateSDK (ids);
    }
}