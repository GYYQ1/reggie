package com.yinqing.reggie.dto;

import com.yinqing.reggie.entity.Dish;
import com.yinqing.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
//数据传输对象
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();
        //用于菜品分页查询中的菜品分类字段
    private String categoryName;

    private Integer copies;
}
