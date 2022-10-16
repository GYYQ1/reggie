package com.yinqing.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinqing.reggie.dto.DishDto;
import com.yinqing.reggie.entity.Dish;
import org.springframework.stereotype.Service;

@Service
public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品的口味数据，需要操作两张表：dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询菜品和对应菜品的口味信息
    public DishDto getByIdWithFlavor(Long id);
    //
    public void updateWithFlavor(DishDto dishDto);
}
