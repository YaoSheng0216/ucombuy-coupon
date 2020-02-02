package com.ucombuy.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;


/**
 * Created by yaosheng on 2020/1/11.
 * Kafka 相关的接口定义
 */
public interface IKafkaService {

    //消费优惠券Kafka的信息
    void consumeCouponKafkaMessage(ConsumerRecord<?,?> record);

}