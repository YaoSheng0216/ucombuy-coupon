package com.ucombuy.coupon.service;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.exception.CouponException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

/**
 * Created by yaosheng on 2020/1/2.
 * 优惠券模版基础服务测试
 */
@SpringBootTest
@RunWith (SpringRunner.class)
public class TemplateBaseTest {

    @Autowired
    private ITemplateBaseService templateBaseService;

    @Test
    public void testBuildTemplateInfo() throws CouponException {

        System.out.println (JSON.toJSONString (templateBaseService.buildTemplateInfo (1)));
        System.out.println (JSON.toJSONString (templateBaseService.buildTemplateInfo (2)));
    }

    @Test
    public void testFindAllUsableTemplate(){

        System.out.println (JSON.toJSONString (templateBaseService.findAllUsableTemplate ()));
    }

    @Test
    public void testFindId2TemplateSDK(){

        System.out.println (JSON.toJSONString (templateBaseService.findIds2TemplateSDK (Arrays.asList (1,2,3))));
    }
}