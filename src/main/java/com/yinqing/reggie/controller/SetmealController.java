package com.yinqing.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinqing.reggie.common.CustomException;
import com.yinqing.reggie.common.R;
import com.yinqing.reggie.dto.DishDto;
import com.yinqing.reggie.dto.SetmealDto;
import com.yinqing.reggie.entity.Category;
import com.yinqing.reggie.entity.Dish;
import com.yinqing.reggie.entity.Setmeal;
import com.yinqing.reggie.entity.SetmealDish;
import com.yinqing.reggie.service.CategoryService;
import com.yinqing.reggie.service.DishService;
import com.yinqing.reggie.service.SetmealDishService;
import com.yinqing.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequestMapping("/setmeal")
@Transactional
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    /***
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String>save(@RequestBody SetmealDto setmealDto){
        setmealService.setWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>>page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Setmeal>pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto>setmealDtoPage=new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
        //添加排序条件
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo,setmealLambdaQueryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        //records里面装的是页面显示的数据，Setmeal对象
        //获取pageInfo里面的records数据
        List<Setmeal> records = pageInfo.getRecords();
        //给每个records里面的categoryId取出来，查询其分类名称

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //复制属性
            BeanUtils.copyProperties(item, setmealDto);
            //获取分类id
            Long categoryId = item.getCategoryId();
            //查询分类对象
            if ((categoryId != null)) {
                Category category = categoryService.getById(categoryId);
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 套餐删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String>delete(@RequestParam List<Long>ids){
        if(ids==null){
            throw new CustomException("请先勾选套餐");
        }
        setmealService.removeWithSetmeal(ids);
        return R.success("套餐删除成功");
    }
@PostMapping("/status/{status}")
    public R<String>updateStatusWithSetmeal(@PathVariable Integer status,@RequestParam List<Long>ids){
                for(Long id:ids){
                    Setmeal setmeal = setmealService.getById(id);
                    setmeal.setStatus(status);
                    setmealService.updateById(setmeal);
                }

                return R.success("套餐状态更新成功");
    }

//    /**
//     * 用户点击前端套餐图片，为了解决前端点套餐404接口异常
//     * @param id
//     * @return
//     */
//    @GetMapping("dish/{id}")
//    public R<String>get(@PathVariable Long id){
//        SetmealDto setmealDto = setmealService.getByIdWithSetmael(id);
//        return R.success("请添加份数");
//    }


    /**
     * 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param SetmealId
     * @return
     */
    //这里前端是使用路径来传值的，要注意，不然你前端的请求都接收不到，就有点尴尬哈
    @GetMapping("/dish/{id}")
    public R<List<DishDto>>dish(@PathVariable("id") Long SetmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
        //获取套餐里面的所有菜品  这个就是SetmealDish表里面的数据
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();
            //其实这个BeanUtils的拷贝是浅拷贝，这里要注意一下
            BeanUtils.copyProperties(setmealDish, dishDto);
            //这里是为了把套餐中的菜品的基本信息填充到dto中，比如菜品描述，菜品图片等菜品的基本信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String>update(@RequestBody SetmealDto setmealDto){
            setmealService.updateWithSetmeal(setmealDto);
        return R.success("更新套餐信息成功");
    }

    /**
     * 查询套餐信息
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>>list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(setmealLambdaQueryWrapper);

        return R.success(list);
    }

    /**
     * 根据id查询套餐信息，用于修改套餐信息时将套餐信息回显浏览器
     * @param id
     * @return
     */
    /**
     * 回显套餐数据：根据套餐id查询套餐
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getData(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithSetmael(id);

        return R.success(setmealDto);
    }
}
