package com.ucombuy.coupon.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Created by yaosheng on 2019/12/31.
 * 自定义异步任务线程池
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncPoolConfig implements AsyncConfigurer {

    @Bean
    @Override
    public Executor getAsyncExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);               //线程数
        executor.setMaxPoolSize(20);                //最大线程池数量
        executor.setQueueCapacity(20);              //队列容量
        executor.setKeepAliveSeconds(60);           //空闲时最长的生存时间
        executor.setThreadNamePrefix("ucombuyAsync_");              //线程名称的前缀
        executor.setWaitForTasksToCompleteOnShutdown(true);         //任务关闭时，线程池是否退出
        executor.setAwaitTerminationSeconds(60);                    //最长等待时间

        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy());         //拒绝策略
        executor.initialize();                                      //初始化线程池

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    @SuppressWarnings("all")             //注释掉所有警告
    class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
            throwable.printStackTrace();
            log.error("AsyncError: {}, Method: {}, Param: {}",
                    throwable.getMessage(), method.getName(),
                    JSON.toJSONString(objects));

            // TODO 在实际的业务中，通常会给运维人员发送邮件或短信，提示错误，做进一步的处理
        }
    }
}