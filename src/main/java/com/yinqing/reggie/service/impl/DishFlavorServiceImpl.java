package com.yinqing.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinqing.reggie.entity.DishFlavor;
import com.yinqing.reggie.mapper.DishFlavorMapper;
import com.yinqing.reggie.service.DishFlavorService;
import com.yinqing.reggie.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
