package com.yinqing.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用于保存和获取当前用户登录id
 */
public class BaseContext {
    public static ThreadLocal<Long>threadLocal=new ThreadLocal<>();
    public static void setCurrent(Long id){
        threadLocal.set(id);
    }
    public static Long getCurrent(){
        return threadLocal.get();
    }

}
