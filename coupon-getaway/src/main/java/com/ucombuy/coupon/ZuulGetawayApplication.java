package com.ucombuy.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Created by yaosheng on 2019/12/29.
 */
@EnableZuulProxy
@SpringBootApplication
public class ZuulGetawayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulGetawayApplication.class, args);
    }
}