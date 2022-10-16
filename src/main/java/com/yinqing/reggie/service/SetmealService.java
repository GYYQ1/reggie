package com.yinqing.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinqing.reggie.dto.DishDto;
import com.yinqing.reggie.dto.SetmealDto;
import com.yinqing.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void setWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联关系
     * @param ids
     */
    public void removeWithSetmeal(List<Long>ids);

    /**
     * 批量停售和启售套餐
     * @param status
     * @param ids
     */
    public void updateStatus(Integer status,List<Long>ids);

    /**
     * 获取套餐信息用于回显浏览器
     * @param id
     * @return
     */
   public SetmealDto getByIdWithSetmael(Long id);

    /**
     * 更新套餐信息
     * @param setmealDto
     */
    public void updateWithSetmeal(SetmealDto setmealDto);
}
