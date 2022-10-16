package com.yinqing.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinqing.reggie.common.BaseContext;
import com.yinqing.reggie.entity.ShoppingCart;
import com.yinqing.reggie.mapper.ShoppingCartMapper;
import com.yinqing.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>implements ShoppingCartService {
    @Override
    public void clean() {
        //获取当前用户的id
        Long currentId = BaseContext.getCurrent();
        //查询当前购物车中的菜品或者套餐，将它们删除
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> list = this.list(queryWrapper);
        list.stream().map((item)->{
            this.removeById(item.getId());
            return item;
        }).collect(Collectors.toList());
    }
}
