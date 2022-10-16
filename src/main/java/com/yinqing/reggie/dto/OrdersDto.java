package com.yinqing.reggie.dto;


import com.yinqing.reggie.entity.OrderDetail;
import com.yinqing.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;
    //商品数量
    private int sumNum;

    private List<OrderDetail> orderDetails;
	
}
