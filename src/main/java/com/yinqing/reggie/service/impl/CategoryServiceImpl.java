package com.yinqing.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinqing.reggie.common.CustomException;
import com.yinqing.reggie.entity.Category;
import com.yinqing.reggie.entity.Dish;
import com.yinqing.reggie.entity.Setmeal;
import com.yinqing.reggie.mapper.CategoryMapper;
import com.yinqing.reggie.service.CategoryService;
import com.yinqing.reggie.service.DishService;
import com.yinqing.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {
    @Autowired
   private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 重新remove方法，判断删除分类时，是否关联了套餐和菜品
     * @param id
     */
    @Override
    public void remove(long id) {
        LambdaQueryWrapper<Dish>dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件,根据分类id进行查询 select count (*) from dish where category_id=?
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联了菜品，如果关联，抛出业务异常
        if (count1>0){
              throw new CustomException("当前分类下关联了菜品，无法删除");
        }

        //查询当前分类是否关联了套餐
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件，根据分类id进行查询 select count(*) from setmeal where category_id=?
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if (count2>0){
              throw new CustomException("当前分类下关联了套餐，无法删除");
        }
        //正常删除
        super.removeById(id);
    }
}
