package com.yinqing.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinqing.reggie.entity.ShoppingCart;
import org.springframework.stereotype.Service;

@Service
public interface ShoppingCartService extends IService<ShoppingCart> {
    //清空购物车
    public void clean();
}
