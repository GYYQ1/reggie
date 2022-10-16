package com.yinqing.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinqing.reggie.entity.Orders;
import org.springframework.stereotype.Service;

@Service
public interface OrderService extends IService<Orders> {
    //用户下单
    public void submit(Orders orders);
}
