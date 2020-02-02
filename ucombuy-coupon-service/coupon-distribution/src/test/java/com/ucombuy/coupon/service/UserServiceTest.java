package com.ucombuy.coupon.service;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.constant.CouponStatus;
import com.ucombuy.coupon.exception.CouponException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by yaosheng on 2020/1/23.
 * 用户服务功能测试用例
 */
@SpringBootTest
@RunWith (SpringRunner.class)
public class UserServiceTest {

    private Long fakeUserId = 20001L;

    @Autowired
    private IUserService iUserService;

    @Test
    public void testFindCouponByStatus() throws CouponException{

        System.out.println (JSON.toJSONString (iUserService.findCouponsByStatus (fakeUserId,
                CouponStatus.USABLE.getCode ())));

        System.out.println (JSON.toJSONString (iUserService.findCouponsByStatus (fakeUserId,
                CouponStatus.USED.getCode ())));

        System.out.println (JSON.toJSONString (iUserService.findCouponsByStatus (fakeUserId,
                CouponStatus.EXPIRED.getCode ())));
    }

    @Test
    public void testFindAvailableTemplate() throws CouponException{

        System.out.println (JSON.toJSONString (iUserService.findAvailableTemplate (fakeUserId)));
    }
}