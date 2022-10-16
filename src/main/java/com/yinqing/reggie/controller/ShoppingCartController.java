package com.yinqing.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinqing.reggie.common.BaseContext;
import com.yinqing.reggie.common.R;
import com.yinqing.reggie.entity.ShoppingCart;
import com.yinqing.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品或者套餐到购物车
     * @return
     */
  @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
      log.info("购物车数据:{}",shoppingCart.toString());

      //获取当前添加购物车的用户的ID，将其保存到shoppingcart中
      Long userId = BaseContext.getCurrent();
      shoppingCart.setUserId(userId);
      shoppingCart.setCreateTime(LocalDateTime.now());

      LambdaQueryWrapper<ShoppingCart>lambdaQueryWrapper=new LambdaQueryWrapper<>();
      lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
      //获取当前添加菜品的id，以判断是添加菜品还是套餐
      Long dishId = shoppingCart.getDishId();
      //判断当前添加的是菜品还是套餐
      if(dishId!=null){
          //添加的是菜品
          lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
      }
      else {
          //添加的是套餐
          lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
      }
      //判断当前用户添加的菜品或套餐是否已经在购物车中，如果已经在购物车中直接number+1，否则添加至购物车中
      //sql:select * from shopping_cart where dish_id=? and user_id=?
      ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);
      if(cartServiceOne!=null){
          //购物车中有数据，number+1
          Integer number = cartServiceOne.getNumber();
          cartServiceOne.setNumber(number+1);
          shoppingCartService.updateById(cartServiceOne);
      }
      else{
          //购物车没有数据，添加至购物车中
          //设置number为1
          shoppingCart.setNumber(1);
          shoppingCartService.save(shoppingCart);
          cartServiceOne=shoppingCart;
      }

      return R.success(cartServiceOne);
    }

    /**
     * 查看购物车的信息
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>>list(){
      //查看购物车
        // 获取当前用户的id
        Long currentId = BaseContext.getCurrent();
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        queryWrapper.ge(ShoppingCart::getNumber,1);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 购物车菜品或者套餐减份数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart>sub(@RequestBody ShoppingCart shoppingCart){
        //获取当前用户的id，以便获取当前用户需要减份的菜品信息
        Long currentId = BaseContext.getCurrent();
        LambdaQueryWrapper<ShoppingCart>lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId);
        //添加条件
        if(shoppingCart.getDishId()!=null) {
            lambdaQueryWrapper.eq( ShoppingCart::getDishId, shoppingCart.getDishId());
            //查询数据
            ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);
            if (cartServiceOne != null) {
                Integer number = cartServiceOne.getNumber();
                number = number - 1;
                if (number <0) {
                    shoppingCartService.removeById(cartServiceOne);
                   return R.error("操作异常");
                } else {
                    cartServiceOne.setNumber(number);
                    shoppingCartService.updateById(cartServiceOne);
                }

            }
            return R.success(cartServiceOne);
        }

        if(shoppingCart.getSetmealId()!=null) {
            lambdaQueryWrapper.eq( ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

            //查询数据
            ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);
            if (cartServiceOne != null) {
                Integer number = cartServiceOne.getNumber();
                number = number - 1;
                if (number <0) {
                    shoppingCartService.removeById(cartServiceOne);
                   return R.error("操作异常");
                } else {
                    cartServiceOne.setNumber(number);
                    shoppingCartService.updateById(cartServiceOne);
                }
            }
            return R.success(cartServiceOne);
        }
        return  R.error("操作异常");
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String>clean(){

shoppingCartService.clean();
      return  R.success("清空购物车成功");
    }

}
