package com.ucombuy.coupon.service;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.constant.CouponCategory;
import com.ucombuy.coupon.constant.DistributeTarget;
import com.ucombuy.coupon.constant.PeriodType;
import com.ucombuy.coupon.constant.ProductLine;
import com.ucombuy.coupon.vo.TemplateRequest;
import com.ucombuy.coupon.vo.TemplateRule;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * Created by yaosheng on 2020/1/2.
 * 构造优惠券模板服务测试
 */
@SpringBootTest
@RunWith (SpringRunner.class)
public class BuildTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void buildTemplate(TemplateRequest request) throws Exception {

        System.out.println (JSON.toJSONString (buildTemplateService.
                buildTemplate (fakeTemplateRequest ())));
        Thread.sleep (5000);
    }

    private TemplateRequest fakeTemplateRequest(){

        TemplateRequest request = new TemplateRequest ();
        request.setName ("优惠券模版-" + new Date ().getTime ());
        request.setLogo ("www.ucombuy.com");
        request.setDesc ("这是一张优惠券模版");
        request.setCategory (CouponCategory.MANJIAN.getCode ());
        request.setProductLine (ProductLine.DAMAO.getCode ());
        request.setCount (10000);
        request.setUserId (1001L);
        request.setTarget (DistributeTarget.SINGLE.getCode ());

        TemplateRule rule = new TemplateRule ();
        rule.setException (new TemplateRule.Exception (PeriodType.SHIFT.getCode (),
                1, DateUtils.addDays (new Date (),60).getTime ()));
        rule.setDiscount (new TemplateRule.Discount (5,1));
        rule.setLimitation (1);
        rule.setUsage (new TemplateRule.Usage ("陕西省","渭南市",
                JSON.toJSONString (Arrays.asList ("文娱","家具"))));
        rule.setWeight (JSON.toJSONString (Collections.EMPTY_LIST));

        request.setRule (rule);
        return request;
    }
}