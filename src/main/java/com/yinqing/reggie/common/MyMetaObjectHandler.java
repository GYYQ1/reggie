package com.yinqing.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
      * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
            log.info("insertFill...公共字段自动填充");
            metaObject.setValue("createTime", LocalDateTime.now());
            metaObject.setValue("updateTime",LocalDateTime.now());
            metaObject.setValue("createUser",BaseContext.getCurrent());
            metaObject.setValue("updateUser",BaseContext.getCurrent());
    }

    /**
     * 更新操作，自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
            log.info("updateFill...公共字段自动填充");
            //通过同一个线程内的变量不变获取
       // long id = Thread.currentThread().getId();
        //log.info("线程为：{}",id);
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrent());
    }
}
