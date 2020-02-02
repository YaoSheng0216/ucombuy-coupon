package com.ucombuy.coupon.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Created by yaosheng on 2020/1/22.
 * Ribbon应用Controller
 */
@Slf4j
@RestController
public class RibbonController {

    //Rest客户端
    @Autowired
    private RestTemplate restTemplate;

    //通过Ribbon组建调用模版微服务
    @GetMapping("/info")
    //@IgnoreResponseAdvice:避免通用响应的包装过程
    public TemplateInfo getTemplateInfo(){

        String infoUrl = "http://eureka-client-coupon-template/coupon-template/info";
        return restTemplate.getForEntity (infoUrl,TemplateInfo.class).getBody ();
    }

    //模版微服务元信息
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TemplateInfo{

        private Integer code;
        private String message;
        private List<Map<String,Object>> data;
    }
}