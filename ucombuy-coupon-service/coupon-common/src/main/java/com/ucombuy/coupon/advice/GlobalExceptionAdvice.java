package com.ucombuy.coupon.advice;

import com.ucombuy.coupon.exception.CouponException;
import com.ucombuy.coupon.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yaosheng on 2020/2/1.
 *全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {

    //对CouponException进行统一处理
    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponException(HttpServletRequest req, CouponException ex) {

        CommonResponse<String> response = new CommonResponse<>(-1, "business error");
        response.setData(ex.getMessage());
        return response;
    }
}