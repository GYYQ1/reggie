package com.yinqing.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinqing.reggie.entity.OrderDetail;
import com.yinqing.reggie.mapper.OrderDetailMapper;
import com.yinqing.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>implements OrderDetailService {
}
