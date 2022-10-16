package com.yinqing.reggie.dto;


import com.yinqing.reggie.entity.Setmeal;
import com.yinqing.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;
//数据传输对象
@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
