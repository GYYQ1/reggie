package com.yinqing.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@EnableCaching    //开启缓存注解功能
@EnableAsync(proxyTargetClass=true)
public class ReggieApplication {
    public static void main(String[] args) {
                SpringApplication.run(ReggieApplication.class,args);
                log.info("项目启动成功...");
    }
}
