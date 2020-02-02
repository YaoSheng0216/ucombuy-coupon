package com.ucombuy.coupon.service;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.constant.CouponCategory;
import com.ucombuy.coupon.constant.GoodsType;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.executor.ExecutorManager;
import com.ucombuy.coupon.vo.CouponTemplateSDK;
import com.ucombuy.coupon.vo.GoodsInfo;
import com.ucombuy.coupon.vo.SettlementInfo;
import com.ucombuy.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by yaosheng on 2020/2/1.
 * 结算规则执行器测试用例
 * 对Executor的分发与结算逻辑进行测试
 */
@Slf4j
@SpringBootTest
@RunWith (SpringRunner.class)
public class ExecutorManagerTest {

    //fake一个userId
    private Long fakeUserId = 20001L;

    @Autowired
    private ExecutorManager manager;

    @Test
    public void testComputeRule() throws CouponException{

        //满减优惠券结算测试
        log.info ("ManJian Coupon Executor Test");
        SettlementInfo manjianInfo = fakeManJianCouponSettlement ();
        SettlementInfo result = manager.computeRule (manjianInfo);

        log.info ("{}",result.getCost ());
        log.info ("{}",result.getCouponAndTemplateInfos ().size ());
        log.info ("{}",result.getCouponAndTemplateInfos ());

        //折扣优惠券结算测试
//        log.info ("ZheKou Coupon Executor Test");
//        SettlementInfo zhekouInfo = fakeZheKouCouponSettlement ();
//        SettlementInfo result = manager.computeRule (zhekouInfo);
//
//        log.info ("{}",result.getCost ());
//        log.info ("{}",result.getCouponAndTemplateInfos ().size ());
//        log.info ("{}",result.getCouponAndTemplateInfos ());

        //立减优惠券结算测试
//        log.info ("LiJian Coupon Executor Test");
//        SettlementInfo lijianInfo = fakeLiJianCouponSettlement ();
//        SettlementInfo result = manager.computeRule (lijianInfo);
//
//        log.info ("{}",result.getCost ());
//        log.info ("{}",result.getCouponAndTemplateInfos ().size ());
//        log.info ("{}",result.getCouponAndTemplateInfos ());

        //满减折扣优惠券结算测试
//        log.info ("ManJian ZheKou Coupon Executor Test");
//        SettlementInfo manjianZheKouInfo = fakeManJianAndZheKouCouponSettlement ();
//        SettlementInfo result = manager.computeRule (manjianZheKouInfo);
//
//        log.info ("{}",result.getCost ());
//        log.info ("{}",result.getCouponAndTemplateInfos ().size ());
//        log.info ("{}",result.getCouponAndTemplateInfos ());
    }

    //fake(mock)满减优惠券结算信息
    private SettlementInfo fakeManJianCouponSettlement(){

        SettlementInfo info = new SettlementInfo ();
        info.setUserId (fakeUserId);
        info.setEmploy (false);
        info.setCost (0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo ();
        goodsInfo01.setCount (2);
        goodsInfo01.setPrice (10.88);
        goodsInfo01.setType (GoodsType.WENYU.getCode ());

        GoodsInfo goodsInfo02 = new GoodsInfo ();
        //达到满减标准
        goodsInfo02.setCount (10);
        //没有达到满减标准
        //goodsInfo02.setCount (5);
        goodsInfo02.setPrice (20.88);
        goodsInfo02.setType (GoodsType.WENYU.getCode ());

        info.setGoodsInfos (Arrays.asList (goodsInfo01,goodsInfo02));
        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo ();
        ctInfo.setId (1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK ();
        templateSDK.setId (1);
        templateSDK.setCategory (CouponCategory.MANJIAN.getCode ());
        templateSDK.setKey ("100120190801");

        TemplateRule rule = new TemplateRule ();
        rule.setDiscount (new TemplateRule.Discount (20,199));
        rule.setUsage (new TemplateRule.Usage ("陕西省","渭南市", JSON.toJSONString
                (Arrays.asList (GoodsType.WENYU.getCode (),GoodsType.JIAJU.getCode ()))));
        templateSDK.setRule (rule);

        ctInfo.setTemplateSDK (templateSDK);
        info.setCouponAndTemplateInfos (Collections.singletonList (ctInfo));

        return info;
    }

    private SettlementInfo fakeZheKouCouponSettlement(){

        SettlementInfo info = new SettlementInfo ();
        info.setUserId (fakeUserId);
        info.setEmploy (false);
        info.setCost (0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo ();
        goodsInfo01.setCount (2);
        goodsInfo01.setPrice (10.88);
        goodsInfo01.setType (GoodsType.WENYU.getCode ());

        GoodsInfo goodsInfo02 = new GoodsInfo ();
        goodsInfo02.setCount (10);
        goodsInfo02.setPrice (20.88);
        goodsInfo02.setType (GoodsType.WENYU.getCode ());

        info.setGoodsInfos (Arrays.asList (goodsInfo01,goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo ();
        ctInfo.setId (1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK ();
        templateSDK.setId (2);
        templateSDK.setCategory (CouponCategory.ZHEKOU.getCode ());
        templateSDK.setKey ("100220200201");

        //设置TemplateRule
        TemplateRule rule = new TemplateRule ();
        rule.setDiscount (new TemplateRule.Discount (85,1));
        rule.setUsage (new TemplateRule.Usage ("北京","北京市",JSON.toJSONString
                (Arrays.asList (GoodsType.WENYU.getCode (),GoodsType.JIAJU.getCode ()))));
        templateSDK.setRule (rule);

        ctInfo.setTemplateSDK (templateSDK);
        info.setCouponAndTemplateInfos (Collections.singletonList (ctInfo));

        return info;
    }

    //立减优惠券结算信息
    private SettlementInfo fakeLiJianCouponSettlement(){

        SettlementInfo info = new SettlementInfo ();
        info.setUserId (fakeUserId);
        info.setEmploy (false);
        info.setCost (0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo ();
        goodsInfo01.setCount (2);
        goodsInfo01.setPrice (10.88);
        goodsInfo01.setType (GoodsType.WENYU.getCode ());

        GoodsInfo goodsInfo02 = new GoodsInfo ();
        goodsInfo02.setCount (10);
        goodsInfo02.setPrice (20.88);
        goodsInfo02.setType (GoodsType.WENYU.getCode ());

        info.setGoodsInfos (Arrays.asList (goodsInfo01,goodsInfo02));
        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo ();
        ctInfo.setId (1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK ();
        templateSDK.setId (3);
        templateSDK.setCategory (CouponCategory.LIJIAN.getCode ());
        templateSDK.setKey ("200320200201");

        TemplateRule rule = new TemplateRule ();
        rule.setDiscount (new TemplateRule.Discount (5,1));
        rule.setUsage (new TemplateRule.Usage ("陕西省","西安市",JSON.toJSONString
                (Arrays.asList (GoodsType.WENYU.getCode (),GoodsType.JIAJU.getCode ()))));
        templateSDK.setRule (rule);
        ctInfo.setTemplateSDK (templateSDK);

        info.setCouponAndTemplateInfos (Collections.singletonList (ctInfo));

        return info;
    }

    private SettlementInfo fakeManJianAndZheKouCouponSettlement(){

        SettlementInfo info = new SettlementInfo ();
        info.setUserId (fakeUserId);
        info.setEmploy (false);
        info.setCost (0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo ();
        goodsInfo01.setCount (2);
        goodsInfo01.setPrice (10.88);
        goodsInfo01.setType (GoodsType.WENYU.getCode ());

        GoodsInfo goodsInfo02 = new GoodsInfo ();
        goodsInfo02.setCount (10);
        goodsInfo02.setPrice (20.88);
        goodsInfo02.setType (GoodsType.WENYU.getCode ());

        info.setGoodsInfos (Arrays.asList (goodsInfo01,goodsInfo02));

        //满减优惠券
        SettlementInfo.CouponAndTemplateInfo manjianInfo = new SettlementInfo.CouponAndTemplateInfo ();
        manjianInfo.setId (1);

        CouponTemplateSDK manjianTemplate = new CouponTemplateSDK ();
        manjianTemplate.setId (1);
        manjianTemplate.setCategory (CouponCategory.MANJIAN.getCode ());
        manjianTemplate.setKey ("100120200201");

        TemplateRule manjianRule = new TemplateRule ();
        manjianRule.setDiscount (new TemplateRule.Discount (20,199));
        manjianRule.setUsage (new TemplateRule.Usage ("陕西省","渭南市",JSON.toJSONString
                (Arrays.asList (GoodsType.WENYU.getCode (),GoodsType.JIAJU.getCode ()))));
        manjianRule.setWeight (JSON.toJSONString (Collections.emptyList ()));
        manjianTemplate.setRule (manjianRule);
        manjianInfo.setTemplateSDK (manjianTemplate);

        //折扣优惠券
        SettlementInfo.CouponAndTemplateInfo zhekouInfo = new SettlementInfo.CouponAndTemplateInfo ();
        zhekouInfo.setId (1);

        CouponTemplateSDK zhekouTemplate = new CouponTemplateSDK ();
        zhekouTemplate.setId (2);
        zhekouTemplate.setCategory (CouponCategory.ZHEKOU.getCode ());
        zhekouTemplate.setKey ("100202020201");

        TemplateRule zhekouRule = new TemplateRule ();
        zhekouRule.setDiscount (new TemplateRule.Discount (85,1));
        zhekouRule.setUsage (new TemplateRule.Usage ("北京","北京市",JSON.toJSONString
                (Arrays.asList (GoodsType.WENYU.getCode (),GoodsType.JIAJU.getCode ()))));
        zhekouRule.setWeight (JSON.toJSONString (Collections.singletonList ("1001202002010001")));
        zhekouTemplate.setRule (zhekouRule);
        zhekouInfo.setTemplateSDK (zhekouTemplate);

        info.setCouponAndTemplateInfos (Arrays.asList (manjianInfo,zhekouInfo));

        return info;
    }
}