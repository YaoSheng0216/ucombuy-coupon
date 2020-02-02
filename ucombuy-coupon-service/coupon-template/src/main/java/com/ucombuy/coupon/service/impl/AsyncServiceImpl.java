package com.ucombuy.coupon.service.impl;

import com.google.common.base.Stopwatch;
import com.ucombuy.coupon.constant.Constant;
import com.ucombuy.coupon.dao.CouponTemplateDao;
import com.ucombuy.coupon.entity.CouponTemplate;
import com.ucombuy.coupon.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yaosheng on 2020/1/1.
 * 异步服务接口实现
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    @Autowired
    private CouponTemplateDao templateDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 根据优惠券模版异步的创建优惠券码
     * @param template  {@link CouponTemplate} 优惠券模版实体
     */
    @Override
    @Async("getAsyncExecutor")           //获取异步线程池
    @SuppressWarnings ("all")
    public void asyncConstructCouponByTemplate(CouponTemplate template) {

        Stopwatch watch = Stopwatch.createStarted ();
        Set<String> couponCodes = buildCouponCode (template);
        //获取Redis Key
        String redisKey = String.format ("%S%s", Constant.RedisPrefix.COUPON_TEMPLATE,template.getId ().toString ());
        log.info ("Push CouponCode To Redis:{}",redisTemplate.opsForList ().rightPushAll (redisKey,couponCodes));
        template.setAvailable (true);
        templateDao.save (template);

        watch.stop ();
        log.info ("Construct Coupon By Template Cost: {}ms",watch.elapsed (TimeUnit.MICROSECONDS));

        // TODO 发送短信或者邮件告诉运营人员优惠券模版已经可以使用
        log.info ("CouponTemplate({}) Is Available!",template.getId ());
    }

    /**
     * 构造优惠券码
     * 优惠券码（对应每一张优惠券，18位）
     * 前四位：产品线 + 产品类型
     * 中间六位：日期随机
     * 后八位：0～9随机数字构成
     * @param template {@link CouponTemplate} 实体类
     * @return Set<String> 与 template.count相同个数的优惠券码
     */
    @SuppressWarnings("all")
    private Set<String> buildCouponCode(CouponTemplate template){

        Stopwatch watch = Stopwatch.createStarted ();         //定时器
        Set<String> result = new HashSet<> (template.getCount ());

        //前四位数字的生成
        String prefix4 = template.getProductLine ().getCode ().toString () + template.getCategory ().getCode ();
        String date = new SimpleDateFormat ("yyMMdd").format (template.getCreateTime ());
        for(int i = 0;i != template.getCount ();i ++){
            result.add (prefix4 + buildCouponCodeSuffix14 (date));
        }

        while(result.size () < template.getCount ()){
            result.add (prefix4 + buildCouponCodeSuffix14 (date));
        }

        assert  result.size () == template.getCount ();
        watch.stop ();
        log.info ("Build Coupon Code Cost: {}ms",watch.elapsed (TimeUnit.MICROSECONDS));
        return result;
    }

    /**
     * 构造优惠券码的后十四位
     * @param data 创建优惠券码的日期
     * @return 14位优惠券码
     */
    private String buildCouponCodeSuffix14(String data){

        char[] bases = new char[]{'1','2','3','4','5','6','7','8','9'};
        //生成中间六位
        List<Character> chars = data.chars ().mapToObj (e -> (char) e).collect(Collectors.toList());    //Java 8的一个lamd表达式
        Collections.shuffle (chars);                   //shuffle是一个洗牌算法
        String mid6 = chars.stream ().map (Objects::toString).collect(Collectors.joining());
        //后八位的生成
        String suffex8 = RandomStringUtils.random (1,bases) + RandomStringUtils.randomNumeric (7);
        return mid6 + suffex8;
    }
}