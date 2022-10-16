package com.yinqing.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinqing.reggie.entity.SetmealDish;
import com.yinqing.reggie.mapper.SetmealDishMapper;
import com.yinqing.reggie.mapper.SetmealMapper;
import com.yinqing.reggie.service.SetmealDishService;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
