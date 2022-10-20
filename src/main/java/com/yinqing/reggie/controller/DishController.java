package com.yinqing.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinqing.reggie.common.CustomException;
import com.yinqing.reggie.common.R;
import com.yinqing.reggie.dto.DishDto;
import com.yinqing.reggie.entity.*;
import com.yinqing.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
  private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String>save(@RequestBody DishDto dishDto){
            log.info(dishDto.toString());
            //因为要操作两个表所以加@Transactional注解，自定义saveWithFlavor方法
            dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page, int pageSize, String name){
       // log.info("页码为：{}，pageSize：{}，name：{}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //records里面装的是页面显示的数据，dish对象
        List<Dish>records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //复制属性
            BeanUtils.copyProperties(item, dishDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
           if(categoryId!=null) {
               Category category = categoryService.getById(categoryId);
               String categoryName = category.getName();
               dishDto.setCategoryName(categoryName);
           }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);

    }

    /**
     * 根据id查询菜品和对应菜品的口味信息,用于修改菜品信息时将菜品信息回显浏览器
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto>get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String>update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        //清理所有缓存在Redis中的数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);
        //清理某个菜品下的数据
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");

    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>>list(Dish dish){
//        //构造条件构造器
//        LambdaQueryWrapper<Dish>dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
//        //添加条件
//        dishLambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        //添加条件，查询status为1的菜品，1为在售，0为停售
//        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
//        //添加排序条件
//        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(dishLambdaQueryWrapper);
//        return R.success(list);
//    }

    @GetMapping("/list")
    public R<List<DishDto>>list(Dish dish){
        List<DishDto> dishDtoList=null;

        //先从Redis中获取缓存数据
        //设置key
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果Redis中有数据，则直接返回数据
        if (dishDtoList!=null){
            return R.success(dishDtoList);
        }
        //如果没有数据，则查询数据库，将数据缓存到Redis中且返回数据
        //构造条件构造器
        LambdaQueryWrapper<Dish>dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        dishLambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询status为1的菜品，1为在售，0为停售
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(dishLambdaQueryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //复制属性
            BeanUtils.copyProperties(item, dishDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            if(categoryId!=null) {
                Category category = categoryService.getById(categoryId);
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String>delete(@RequestParam List<Long>ids){
        //构造构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件,在售的菜品不能删除
        //select count(*) from dish where id in(1,2,3)and status=1
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        int count = dishService.count(dishLambdaQueryWrapper);
        //判断是否有在售的菜品
        if(count>0){
            throw new CustomException("菜品正在售卖中，不能删除");
        }
        //没有在售菜品，则可以删除菜品
        dishService.removeByIds(ids);
        return R.success("删除菜品成功");
    }

    /**
     * 批量启售和停售菜品
     * @param status
     * @param ids
     * @return
     */

    @PostMapping("/status/{status}")
    @Transactional
    public R<String>updateStatusWithDish(@PathVariable("status") Integer status,@RequestParam List<Long>ids){
            //停售菜品时，包含菜品的套餐也要停售；
        for(Long id:ids) {
            if (id != null) {
                Dish dish = dishService.getById(id);
                dish.setStatus(status);
                dishService.updateById(dish);

                ///停售菜品时，包含菜品的套餐也要停售；
//                if (status == 0) {
//                    //select * from setmeal_dish where dish_id=id
//                    LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
//                    setmealDishLambdaQueryWrapper.eq(SetmealDish::getDishId, id);
//                    //找到包含该菜品的套餐菜品的集合
//                    List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishLambdaQueryWrapper);
//                    //找到包含该菜品的套餐id的集合
//                    if (setmealDishes != null) {
//                        List<Long> setmealIds = setmealDishes.stream().map((SetmealDish::getSetmealId)).distinct().collect(Collectors.toList());
//                        //执行更新
//                        if (setmealIds != null) {
//                            setmealService.updateStatus(status, setmealIds);
//                        }
//                    }
//                }
           }

        }

        return R.success("更新菜品状态成功");
    }
}
