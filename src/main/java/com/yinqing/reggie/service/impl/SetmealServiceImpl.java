package com.yinqing.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinqing.reggie.common.CustomException;
import com.yinqing.reggie.dto.DishDto;
import com.yinqing.reggie.dto.SetmealDto;
import com.yinqing.reggie.entity.Setmeal;
import com.yinqing.reggie.entity.SetmealDish;
import com.yinqing.reggie.mapper.SetmealMapper;
import com.yinqing.reggie.service.SetmealDishService;
import com.yinqing.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>implements SetmealService {
    @Autowired
   private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系，@Transactional注解，要么事务执行全成功，要么全失败
     * @param setmealDto
     */
    @Override
    @Transactional
    public void setWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> list = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作

        setmealDishService.saveBatch(list);
    }
    /**
     * 删除套餐，同时需要删除套餐和菜品的关联关系
     * @param ids
     */
    public void removeWithSetmeal(List<Long>ids){
        //查询套餐状态，确定是否可以删除，状态1代表在售
        //select count(*) from setmeal where id in(1,2,3) and status =1
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        setmealLambdaQueryWrapper.in(Setmeal::getId,ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,1);

        int count=this.count(setmealLambdaQueryWrapper);
        //判断是否有在售数据不能删除
        if(count>0){
           throw new CustomException("套餐正在售卖中，不能删除");
        }
        //没有在售数据，则可以删除
        //先删除套餐信息，再删除套餐与菜品的关联信息
        //删除setmeal表里的信息
        this.removeByIds(ids);
        //删除setmeal_dish表里面的信息
        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //执行删除
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    /**
     * 批量停售和启售套餐
     * @param status
     * @param ids
     */
    @Override
    @Transactional
    public void updateStatus(Integer status, List<Long> ids) {
        //update setmeal set status=? where id=?;

         //构造更新构造器
         LambdaUpdateWrapper<Setmeal>setmealLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
         //构造条件
         setmealLambdaUpdateWrapper.set(Setmeal::getStatus,status).in(Setmeal::getId,ids);

         this.update(setmealLambdaUpdateWrapper);

    }

    /**
     * 获取套餐的分类名称和套餐中含有的菜品，用于套餐修改时的套餐分类名称回显
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithSetmael(Long id) {
        //查询套餐基本信息，从setmeal表查询
        Setmeal setmeal = this.getById(id);
        //查询当前套餐对应的菜品信息，从setmeal_dish表查询
        SetmealDto setmealDto = new SetmealDto();

        LambdaQueryWrapper<SetmealDish>setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(id!=null,SetmealDish::getSetmealId,id);
        if (setmeal!=null){
            //继承的属性只能用复制赋值
            BeanUtils.copyProperties(setmeal,setmealDto);
            List<SetmealDish> list = setmealDishService.list(setmealDishLambdaQueryWrapper);
            setmealDto.setSetmealDishes(list);
            return setmealDto;
        }
        return null;
    }

    /**
     * 更新套餐信息
     * @param setmealDto
     */
    public void updateWithSetmeal(SetmealDto setmealDto){
        //更新setmeal表基本信息
        this.updateById(setmealDto);
        //更新套餐对应的菜品信息，操作setmeal_dish表
        LambdaQueryWrapper<SetmealDish>setmealDishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        //删除原先的套餐对应的菜品数据
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
        //添加页面新提交过来的数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //setmeal_id为封装,浏览器传过来的数据没有这个，只要分类名称
        setmealDishes=setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);

    }
}
