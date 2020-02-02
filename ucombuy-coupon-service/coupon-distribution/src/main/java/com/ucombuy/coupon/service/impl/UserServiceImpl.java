package com.ucombuy.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.constant.Constant;
import com.ucombuy.coupon.constant.CouponStatus;
import com.ucombuy.coupon.dao.CouponDao;
import com.ucombuy.coupon.entity.Coupon;
import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.feign.Settlementclient;
import com.ucombuy.coupon.feign.TemplateClient;
import com.ucombuy.coupon.service.IRedisService;
import com.ucombuy.coupon.service.IUserService;
import com.ucombuy.coupon.vo.*;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by yaosheng on 2020/1/20.
 * 用户服务相关的接口实现
 * 所有的操作过程，状态都保存在Redis中，并通过Kafka把消息传递到MySQL中
 * 使用Kafka而不使用SpringBoot中的异步处理，最重要的是出于安全方面的考虑，异步任务可能会出现失败的情况，而传入到Kafka
   中的消息，可以重新从Kafka中去获取消息，回溯记录，保证Cache中的一致性，系统的所有操作都基于Redis来实现
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    //报红为Idea无法识别造成，可以忽略
    @Autowired
    private CouponDao couponDao;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private TemplateClient templateClient;

    @Autowired
    private Settlementclient settlementclient;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    /**
     * 根据用户id和状态查询优惠券记录
     * @param userId 用户id
     * @param status 优惠券状态
     * @return {@link Coupon}
     * @throws CouponException
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {

        List<Coupon> curCached = redisService.getCacheCoupons (userId, status);
        List<Coupon> preTarget;

        if(CollectionUtils.isNotEmpty (curCached)){

            log.debug ("coupon cache is not empty : {},{}",userId,status);
            preTarget = curCached;
        }else{

            log.debug ("coupon cache is empty,get coupon from db : {},{}",userId,status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus (userId, CouponStatus.of (status));
            //如果数据库中没有记录，直接返回就可以，Cache中已经添加了一张无效的优惠券
            if(CollectionUtils.isEmpty (dbCoupons)){

                log.debug ("current user do not have coupon : {},{}",userId,status);
                return dbCoupons;
            }
            //填充dbCoupons的templateSDK字段
            Map<Integer,CouponTemplateSDK> id2TemplateSDK = templateClient.findIds2TemplateSDK (dbCoupons.stream ().
                    map (Coupon::getTemplateId).collect(Collectors.toList())).getData ();
            dbCoupons.forEach (dc ->dc.setTemplateSDK (id2TemplateSDK.get(dc.getTemplateId ())));

            //数据库中存在记录
            preTarget = dbCoupons;
            //将记录写入到Cache中
            redisService.addCouponToCache (userId,preTarget,status);
        }

        //将无效的优惠券剔除
        preTarget = preTarget.stream ().filter (c -> c.getId () != -1).collect(Collectors.toList());
        //如果当前获取的是可用的优惠券，需要对已过期的优惠券做延迟处理
        if(CouponStatus.of (status) == CouponStatus.USABLE){

            CouponClassify classify = CouponClassify.classify (preTarget);
            //如果已过期状态不为空，需要做延迟处理
            if(CollectionUtils.isNotEmpty (classify.getExpired ())){

                log.info ("Add Expired coupons To Cache From FindCouponsByStatus : " + "{},{}",userId,status);
                redisService.addCouponToCache (userId,classify.getExpired (),CouponStatus.EXPIRED.getCode ());
                //发送到Kafka中做异步处理
                kafkaTemplate.send (Constant.TOPIC, JSON.toJSONString (new CouponKafkaMessage
                        (CouponStatus.EXPIRED.getCode (),classify.getExpired ().stream ().
                                map (Coupon::getId).collect(Collectors.toList()))));
            }
            return classify.getUsable ();
        }
        return preTarget;
    }

    /**
     * 根据用户id查找当前可以领取的优惠券模版
     * @param userId
     * @return {@link CouponTemplateSDK}
     * @throws CouponException
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {

        long curTime = new Date ().getTime ();
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate ().getData ();
        log.debug ("Find All Template(From TemplateClient) Count : {}",templateSDKS.size ());
        //过滤过期的优惠券模版
        templateSDKS = templateSDKS.stream ().filter (t -> t.getRule ().getException ().
                getDeadline () > curTime).collect(Collectors.toList());
        log.info ("Find Usable Template count : {}",templateSDKS.size ());

        //key是TemplateId，value中的key是Template limitation，right是优惠券模版
        Map<Integer, Pair<Integer,CouponTemplateSDK>> limit2Template = new HashMap<> (templateSDKS.size ());
        //templateSDKS.forEach(t -> limit2Template.put(t.getId(), Pair.of(t.getRule().getLimitation(), t)));
        List<CouponTemplateSDK> result = new ArrayList<> (limit2Template.size ());
        List<Coupon> userUsableCoupons = findCouponsByStatus (userId,CouponStatus.USABLE.getCode ());

        log.debug ("Current User Has Usable Coupons : {},{}",userId,userUsableCoupons.size ());
        //key是TemplateId
        Map<Integer,List<Coupon>> templateId2Coupons = userUsableCoupons.stream ().
                collect (Collectors.groupingBy(Coupon::getTemplateId));
        //根据Template的注入判断是否可以领取优惠券模版
//        limit2Template.forEach ((k,v) -> {
//
//            int limitation = v.getLift();
//            CouponTemplateSDK templateSDK = v.getRight();
//
//            if(templateId2Coupons.containsKey (k) && templateId2Coupons.get (k).size () >= limitation){
//                return;
//            }
//            result.add (templateSDK);
//        });
        return result;
    }

    /**
     * 用户领取优惠券
     * 1.从TemplateClient拿到对应的优惠券，并检查是否过期
     * 2.根据limitation判断用户是否可以领取
     * 3.save to db
     * 4.填充CouponTemplateSDK
     * 5.save to cache
     * @param request
     * @return {@link Coupon}
     * @throws CouponException
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {

        //获取用户可以领取的优惠券模版
        Map<Integer,CouponTemplateSDK> id2Template = templateClient.findIds2TemplateSDK
                (Collections.singletonList (request.getTemplateSDK ().getId ())).getData ();
        //优惠券模版需要存在
        if(id2Template.size () <= 0){

            log.error ("Can Not Acquire Template From TemplateClient : {}",request.getTemplateSDK ().getId ());
            throw new CouponException("Can Not Acquire Template From TemplateClient");
        }
        //用户是否可以领取这张优惠券模版
        List<Coupon> userUsableCoupons = findCouponsByStatus (request.getUserId (),CouponStatus.USABLE.getCode ());
        Map<Integer,List<Coupon>> templateId2Coupons = userUsableCoupons.stream ().collect
                (Collectors.groupingBy (Coupon::getTemplateId));

        if(templateId2Coupons.containsKey (request.getTemplateSDK ().getId ()) && templateId2Coupons.get
                (request.getTemplateSDK ().getId ()).size () >= request.getTemplateSDK ().getRule ().getLimitation ()){

            log.error ("Exceed Template Assign Limitation : {}",request.getTemplateSDK ().getId ());
            throw new CouponException ("Exceed Template Assign Limitation");
        }

        //尝试获取优惠券模版
        String couponCode = redisService.tryToAcquireCouponCodeFromCache (request.getTemplateSDK ().getId ());
        if(StringUtils.isEmpty (couponCode)){

            log.error ("Can Not Acquire Coupon Code : {}",request.getTemplateSDK ().getId ());
            throw new CouponException ("Can Not Acquire Coupon Code");
        }

        Coupon newCoupon = new Coupon (request.getTemplateSDK ().getId (),request.getUserId (),
                couponCode,CouponStatus.USABLE);
        //newCoupon = couponDao.save (newCoupon);

        //填充Coupon对象的CouponTemplateSDK，一定要放在缓存之前填充
        newCoupon.setTemplateSDK (request.getTemplateSDK ());

        //放入到缓存中
        redisService.addCouponToCache (request.getUserId (),Collections.singletonList
                (newCoupon),CouponStatus.USABLE.getCode ());

        return newCoupon;
    }

    /**
     * 结算（核销）优惠券
     * 规则相关处理由Settlement系统去做，当前系统仅仅去做业务处理过程（校验过程）
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     * @throws CouponException
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {

        //当没有传递优惠券时,直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = info.getCouponAndTemplateInfos();
        if (CollectionUtils.isEmpty(ctInfos)) {

            log.info("Empty Coupons For Settle");
            double goodsSum = 0.0;

            for (GoodsInfo gi : info.getGoodsInfos()) {
                goodsSum += gi.getPrice() + gi.getCount();
            }

            //没有优惠券也就不存在优惠券的核销,SettlementInfo其他的字段不需要修改
            info.setCost(retain2Decimals(goodsSum));
        }

        //校验传递的优惠券是否是用户自己的
        List<Coupon> coupons = findCouponsByStatus(info.getUserId(),CouponStatus.USABLE.getCode());
        Map<Integer, Coupon> id2Coupon = coupons.stream().collect(Collectors.
                toMap(Coupon::getId,Function.identity()));

        if (MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(ctInfos.stream().
                map(SettlementInfo.CouponAndTemplateInfo::getId).collect(Collectors.toList()),id2Coupon.keySet())) {

            log.info("{}", id2Coupon.keySet());
            log.info("{}", ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId).
                    collect(Collectors.toList()));
            log.error("User Coupon Has Some Problem, It Is Not SubCollection" + "Of Coupons");

            throw new CouponException("User Coupon Has Some Problem," + "It Is Not SubCollection Of Coupons");
        }

        log.debug("Current Settlement Coupons Is User's : {}",ctInfos.size());

        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ci -> settleCoupons.add(id2Coupon.get(ci.getId())));

        //通过结算服务获取结算信息
        SettlementInfo processedInfo = settlementclient.computeRule(info).getData();
        if (processedInfo.getEmploy() && CollectionUtils.isNotEmpty(processedInfo.getCouponAndTemplateInfos())) {

            log.info("Settle User Coupon: {},{}",info.getUserId(), JSON.toJSONString(settleCoupons));
            //更新缓存
            redisService.addCouponToCache(info.getUserId(), settleCoupons,CouponStatus.USED.getCode());

            //更新DB
            kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(CouponStatus.USED.getCode(),
                    settleCoupons.stream().map(Coupon::getId).collect(Collectors.toList()))));
        }
        return processedInfo;
    }

    //保留两位小数
    private double retain2Decimals(double value) {

        //BigDecimal.ROUND_HALF_UP代表四舍五入
        return new BigDecimal (value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}