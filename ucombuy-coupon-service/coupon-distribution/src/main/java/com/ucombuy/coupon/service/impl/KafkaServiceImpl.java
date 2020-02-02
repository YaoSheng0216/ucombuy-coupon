package com.ucombuy.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.ucombuy.coupon.constant.Constant;
import com.ucombuy.coupon.constant.CouponStatus;
import com.ucombuy.coupon.dao.CouponDao;
import com.ucombuy.coupon.entity.Coupon;
import com.ucombuy.coupon.service.IKafkaService;
import com.ucombuy.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Created by yaosheng on 2020/1/14.
 * Kafka相关的服务接口实现
 * 核心思想:将Cache中的Coupon状态变化同步到数据库中
 */
@Slf4j
@Service
public class KafkaServiceImpl implements IKafkaService {

    @Autowired
    private CouponDao couponDao;

    //消费优惠券Kafka信息
    @Override
    //对Kafka中的设置好的Topic进行监听
    @KafkaListener(topics = {Constant.TOPIC},groupId = "ucombuy-coupon-1")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {

        Optional<?> kafkaMessage = Optional.ofNullable (record.value ());
        if(kafkaMessage.isPresent ()){

            Object message = kafkaMessage.get ();
            CouponKafkaMessage couponInfo = JSON.parseObject (message.toString (),CouponKafkaMessage.class);
            log.info ("Receive CouponKafkaMessage :{}",message.toString ());
            CouponStatus status = CouponStatus.of (couponInfo.getStatus ());

            switch (status){
                case USABLE:
                    break;
                case USED:
                    break;
                case EXPIRED:
                    break;
            }
        }
    }

    //处理已使用的用户优惠券
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage, CouponStatus status) {

        // TODO 给用户发送短信
        processCouponsByStatus(kafkaMessage, status);
    }

    //处理过期的用户优惠券
    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage, CouponStatus status) {

        // TODO 给用户发送推送
        processCouponsByStatus(kafkaMessage, status);
    }

    //根据状态处理优惠券信息
    private void processCouponsByStatus(CouponKafkaMessage kafkaMessage,CouponStatus status){

        List<Coupon> coupons = couponDao.findAllById (kafkaMessage.getIds ());
        if(CollectionUtils.isEmpty (coupons) ||
                coupons.size () != kafkaMessage.getIds ().size ()){
            log.error ("Can Not Find Right Coupon Info : {}",JSON.toJSONString (kafkaMessage));

            //TODO 发送邮件
            coupons.forEach (c -> c.setStatus (status));
            log.info ("CouponKafkaMessage Op Coupon Count : {}",couponDao.saveAll (coupons).size ());
        }
    }
}