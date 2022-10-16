package com.yinqing.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinqing.reggie.common.BaseContext;
import com.yinqing.reggie.common.R;
import com.yinqing.reggie.dto.OrdersDto;
import com.yinqing.reggie.entity.OrderDetail;
import com.yinqing.reggie.entity.Orders;
import com.yinqing.reggie.entity.ShoppingCart;
import com.yinqing.reggie.service.OrderDetailService;
import com.yinqing.reggie.service.OrderService;
import com.yinqing.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
@Transactional
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 订单分页查询和条件查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,Long number,String beginTime,String endTime){

        //分页构造器
        Page<Orders>pageInfo=new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Orders>ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        ordersLambdaQueryWrapper.eq(number!=null,Orders::getNumber,number);
        //大于beginTime,小于endTime,ge大于，le小于
        ordersLambdaQueryWrapper.ge(beginTime!=null,Orders::getOrderTime,beginTime);
        ordersLambdaQueryWrapper.le(endTime!=null,Orders::getOrderTime,endTime);
        //添加排序条件
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        //执行查询
        orderService.page(pageInfo,ordersLambdaQueryWrapper);

        return R.success(pageInfo);

    }
        @PutMapping
    public R<String>updateStatus(@RequestBody Orders orders){
            Orders order = orderService.getById(orders.getId());
            //根据不同的状态返回不同的提示语
            if(orders.getStatus()==3){
                order.setStatus(3);
                orderService.updateById(order);
              return   R.success("订单配送成功");
            }

            else{
                order.setStatus(4);
                orderService.updateById(order);
              return   R.success("订单已完成");
            }
        }

    /**
     * 用户下单
     * @return
     */
    @PostMapping("/submit")
        public R<String>submit(@RequestBody Orders orders){
                orderService.submit(orders);
        return R.success("支付成功");
        }

    /**
     * 查看订单
     * @return
     */
    @GetMapping("/userPage")
        public R<Page>userPage(int page,int pageSize){
        //获取用户id,前端需要订单信息及详细订单信息
        Long userId = BaseContext.getCurrent();
        //分页构造器
        Page<Orders>pageInfo=new Page<>(page,pageSize);
        Page<OrdersDto>ordersDtoPage=new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Orders>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId);
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //执行查询
        orderService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        //records里面装的是页面显示的数据，Orders对象
        List<Orders> ordersList = pageInfo.getRecords();

        List<OrdersDto> list = ordersList.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //属性赋值
            BeanUtils.copyProperties(item, ordersDto);
            //orders表里的id
            Long id = item.getId();
            //根据id查订单号
            Orders orders = orderService.getById(id);
            String number = orders.getNumber();
            //根据订单号查order_detail表
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, number);
            wrapper.ge(OrderDetail::getNumber,1);
            //查询执行
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper);
            ordersDto.setOrderDetails(orderDetails);
            //累加计算商品总数
            int num = 0;
            for (OrderDetail orderDetail : orderDetails) {
                num += orderDetail.getNumber().intValue();
            }
            ordersDto.setSumNum(num);
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(list);
        return R.success(ordersDtoPage);
        }

    /**
     * 再来一单
     * @param order1
     * @return
     */

    //客户端点击再来一单
    /**
     * 前端点击再来一单是直接跳转到购物车的，所以为了避免数据有问题，再跳转之前我们需要把购物车的数据给清除
     * ①通过orderId获取订单明细
     * ②把订单明细的数据的数据塞到购物车表中，不过在此之前要先把购物车表中的数据给清除(清除的是当前登录用户的购物车表中的数据)，
     * 不然就会导致再来一单的数据有问题；
     * (这样可能会影响用户体验，但是对于外卖来说，用户体验的影响不是很大，电商项目就不能这么干了)
     */
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String,String> map){
        String ids = map.get("id");

        long id = Long.parseLong(ids);

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        //获取该订单对应的所有的订单明细表
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
        shoppingCartService.clean();

        //获取用户id
        Long userId = BaseContext.getCurrent();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null) {
                //如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                //添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }

}
