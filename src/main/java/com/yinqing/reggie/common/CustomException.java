package com.yinqing.reggie.common;

/**
 * 自定义异常类，用于删除分类时，是否关联了套餐和菜品
 */
public class CustomException extends RuntimeException{
    public CustomException(String msg){
        super(msg);
    }
}
