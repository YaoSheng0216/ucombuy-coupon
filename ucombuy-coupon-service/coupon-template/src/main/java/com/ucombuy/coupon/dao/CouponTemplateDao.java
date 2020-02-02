package com.ucombuy.coupon.dao;

import com.ucombuy.coupon.entity.CouponTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Created by yaosheng on 2019/12/30.
 * CouponTemplate dao 接口定义
 */
@Component
public interface CouponTemplateDao extends JpaRepository<CouponTemplate,Integer> {

    /**
     * 根据模板名称查询模板
     * where name = ...
     * */
    CouponTemplate findByName(String name);

    /**
     * 根据 available 和 expired 标记查找模板记录
     * where available = ... and expired = ...
     * */
    List<CouponTemplate> findAllByAvailableAndExpired(Boolean available, Boolean expired);

    /**
     * 根据 expired 标记查找模板记录
     * where expired = ...
     * */
    List<CouponTemplate> findAllByExpired(Boolean expired);

    CouponTemplate save(CouponTemplate template);

    Optional<CouponTemplate> findById(Integer id);
}