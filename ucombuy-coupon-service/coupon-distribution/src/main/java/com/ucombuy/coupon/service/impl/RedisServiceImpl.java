package com.ucombuy.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.constant.Constant;
import com.ucombuy.coupon.constant.CouponStatus;
import com.ucombuy.coupon.entity.Coupon;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yaosheng on 2020/1/12.
 * Redis相关操作服务接口实现
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 根据userId和状态找到缓存的优惠券列表数据
     * @param userId 用户id
     * @param status 优惠券状态
     * @return {@link Coupon}s,注意 : 可能会返回null，代表从没有过记录
     */
    @Override
    public List<Coupon> getCacheCoupons(Long userId, Integer status) {

        log.info ("Get Coupons From Cache : {} , {}",userId,status);
        String redisKey = status2RedisKey (status, userId);
        List<String> couponStrs = redisTemplate.opsForHash ().values (redisKey).stream ()
                .map(o -> Objects.toString (o,null)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty (couponStrs)){

            saveEmptyCouponListToCache (userId, Collections.singletonList (status));
            return Collections.emptyList ();
        }
        return couponStrs.stream ().map (cs -> JSON.parseObject (cs,Coupon.class)).collect (Collectors.toList ());
    }

    /**
     * 保存空的优惠券列表到缓存中
     * 目的 : 避免缓存穿透
     * @param userId
     * @param status
     */
    @Override
    @SuppressWarnings ("all")
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {

        log.info ("Save Empty List To Cache For User: {},Status: {}",userId, JSON.toJSONString (status));

        //key是coupon_id，value是序列化的Coupon
        Map<String,String> invalidCouponMap = new HashMap<> ();
        invalidCouponMap.put ("-1",JSON.toJSONString (Coupon.invalidCoupon ()));

        //使用Redis中的SessionCallback把数据命令放入到Redis的pipeline
        SessionCallback<Object> sessionCallback = new SessionCallback<Object> () {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                status.forEach (s -> {
                    String redisKey = status2RedisKey (s,userId);
                    redisOperations.opsForHash ().putAll (redisKey,invalidCouponMap);
                });
                return null;
            }
        };
        log.info ("Pipeline Exe Result: {}",JSON.toJSONString (redisTemplate.executePipelined (sessionCallback)));
    }

    //尝试从Cache中获取一个优惠券码
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {

        String redisKey = String.format ("%s%s",Constant.RedisPrefix.COUPON_TEMPLATE,templateId.toString ());
        //因为优惠券码不存在顺序关系，所以左边pop或者右边pop都没有影响
        String couponCode = redisTemplate.opsForList ().leftPop (redisKey);
        log.info ("Acquire Coupon Code : {},{},{}",templateId,redisKey,couponCode);

        return couponCode;
    }

    //将用户的优惠券码保存到cache中
    @Override
    public Integer addCouponToCache(Long usrId, List<Coupon> coupons, Integer status) throws CouponException {

        log.info ("Add Coupon To Cache : {},{},{}",usrId,JSON.toJSONString (coupons),status);
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of (status);
        switch (couponStatus){

            case USED:
                result = addCouponToCacheForUsed (usrId,coupons);
                break;
            case USABLE:
                result = addCouponToCacheForUsable (usrId,coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired (usrId,coupons);
                break;
        }
        return result;
    }

    //新增加优惠券到cache中
    private Integer addCouponToCacheForUsable(Long userId,List<Coupon> coupons){

        //如果status是USABLE，代表是新增加的优惠券，只会影响一个Cache
        log.debug ("Add Coupon To Cache For Usable");
        Map<String,String> needCacheObject = new HashMap<> ();
        coupons.forEach (c -> needCacheObject.put (c.getId ().toString (),JSON.toJSONString (c)));
        String redisKey = status2RedisKey (CouponStatus.USABLE.getCode (),userId);
        redisTemplate.opsForHash ().putAll (redisKey,needCacheObject);
        log.info ("Add {} Coupons To Cache : {},{}",needCacheObject.size (),userId,redisKey);
        redisTemplate.expire (redisKey,getRandomExpirationTime (1,2), TimeUnit.SECONDS);

        return needCacheObject.size ();
    }

    //将已使用的优惠券添加到Cache中
    @SuppressWarnings ("all")
    private Integer addCouponToCacheForUsed(Long userId,List<Coupon> coupons) throws CouponException{

        //如果status是USED，代表用户操作当前的优惠券，影响到两个Cache : USABLE,USED
        log.debug ("Add Coupon To Cache For Used");
        Map<String,String> needCacheForUsed = new HashMap<> (coupons.size ());
        String redisKeyForUsable = status2RedisKey (CouponStatus.USABLE.getCode (),userId);
        String redisKeyForUsed = status2RedisKey (CouponStatus.USED.getCode (),userId);
        //获取当前用户可用的优惠券
        List<Coupon> curUsableCoupon = getCacheCoupons (userId,CouponStatus.USABLE.getCode ());
        //判断当前可用的优惠券个数一定是大于1
        assert  curUsableCoupon.size () > coupons.size ();
        coupons.forEach (c -> needCacheForUsed.put (c.getId ().toString (),JSON.toJSONString (c)));
        //校验当前的优惠券参数是否与Cache中的匹配
        List<Integer> curUSableIds = curUsableCoupon.stream ().map (Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream ().map (Coupon::getId).collect(Collectors.toList());

        if(!org.apache.commons.collections4.CollectionUtils.isSubCollection (paramIds,curUSableIds)){

            log.error ("CurCoupons Is Not Equal To Cache : {},{},{}",userId,
                    JSON.toJSONString (curUSableIds),JSON.toJSONString (paramIds));
            throw new CouponException ("CurCoupons Is Not Equal To Cache");
        }

        List<String> needCleanKey = paramIds.stream ().map (i -> i.toString ()).collect (Collectors.toList ());

        SessionCallback<Object> sessionCallback = new SessionCallback<Object> () {

            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                //1.已使用的优惠券Cache缓存添加
                redisOperations.opsForHash ().putAll (redisKeyForUsable,needCacheForUsed);
                //2.可用的优惠券Cache，需要清理
                redisOperations.opsForHash ().delete (redisKeyForUsable,needCleanKey.toArray ());
                //3.重置过期时间
                redisOperations.expire (redisKeyForUsable,getRandomExpirationTime (1,2),TimeUnit.SECONDS);
                redisOperations.expire (redisKeyForUsed,getRandomExpirationTime (1,2),TimeUnit.SECONDS);

                return null;
            }
        };

        log.info ("Pipeline Exe Result : {}",JSON.toJSONString
                (redisTemplate.executePipelined (sessionCallback)));
        return coupons.size ();
    }

    //将过期的优惠券添加到Cache中
    @SuppressWarnings ("all")
    private Integer addCouponToCacheForExpired(Long userId,List<Coupon> coupons) throws CouponException {

        //status是EXPIRED，代表是已有的优惠券过期，影响到两个Cache : USABLE,EXPIRE
        log.debug ("Add Coupon To Cache For Expired");
        //保存最终需要的Cache
        Map<String,String> needCacheedForExpired = new HashMap<> (coupons.size ());
        String redisKeyForUsable = status2RedisKey (CouponStatus.USABLE.getCode (),userId);
        String redisKeyForExpired = status2RedisKey (CouponStatus.EXPIRED.getCode (),userId);

        List<Coupon> curUsableCoupons = getCacheCoupons (userId,CouponStatus.USABLE.getCode ());
        List<Coupon> curExpiredCoupons = getCacheCoupons (userId,CouponStatus.EXPIRED.getCode ());

        //当前可用的优惠券数量一定是大于1
        assert curExpiredCoupons.size () > coupons.size ();
        coupons.forEach (c -> needCacheedForExpired.put (c.getId ().toString (),JSON.toJSONString (c)));

        //校验当前的优惠券参数属否与Cache中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream ()
                .map (Coupon::getId).collect (Collectors.toList ());
        List<Integer> paramIds = coupons.stream ()
                .map (Coupon::getId).collect (Collectors.toList ());

        if(!org.apache.commons.collections4.CollectionUtils.isSubCollection (paramIds,curUsableCoupons)){

            log.error ("CurCoupons Is Not Equal To Cache : {},{},{}",
                    userId,JSON.toJSONString (curUsableIds),JSON.toJSONString (paramIds));
            throw new CouponException ("Coupon Is Not Equal To Cache");
        }

        List<String> needCleanKey = paramIds.stream ().map (i -> i.toString ()).collect (Collectors.toList ());

        SessionCallback sessionCallback = new SessionCallback () {

            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                //1.已过期的优惠券缓存
                redisOperations.opsForHash ().putAll (redisKeyForExpired,needCacheedForExpired);
                //2.可用的优惠券Cache需要清理
                redisOperations.opsForHash ().delete (redisKeyForUsable,needCleanKey.toArray ());
                //3.重置过期时间
                redisOperations.expire (redisKeyForUsable,getRandomExpirationTime (1,2),TimeUnit.SECONDS);
                redisOperations.expire (redisKeyForExpired,getRandomExpirationTime (1,2),TimeUnit.SECONDS);

                return null;
            }
        };

        log.info ("Pipeline Exe Result : {}",JSON.toJSONString
                (redisTemplate.executePipelined (sessionCallback)));
        return coupons.size ();
    }

    //根据status获取到对应的Redis Key
    private String status2RedisKey(Integer status,Long userId){

        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of (status);
        switch (couponStatus){

            case USABLE:
                redisKey = String.format ("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE,userId);
                break;
            case USED:
                redisKey = String.format ("%s%s",Constant.RedisPrefix.USER_COUPON_USED,userId);
                break;
            case EXPIRED:
                redisKey = String.format ("%s%s",Constant.RedisPrefix.USER_COUPON_EXPIRED,userId);
                break;
        }
        return redisKey;
    }

    /**
     * 获取一个随机的过期时间
     * 缓存雪崩:Key在同一时间失效
     * @param min 最小的时间数
     * @param max 最大的时间数
     * @return 返回 [min,max] 之间的随机秒数
     */
    private Long getRandomExpirationTime(Integer min,Integer max){

        return RandomUtils.nextLong (min * 60 * 60,max * 60 * 60);
    }
}