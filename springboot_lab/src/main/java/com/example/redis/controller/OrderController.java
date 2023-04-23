package com.example.redis.controller;

import com.example.redis.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@Api(tags = "订单接口")
public class OrderController {

    @Resource
    OrderService orderService;

    @ApiOperation("新增订单")
    @RequestMapping(value = "/order/add", method = RequestMethod.POST)
    public void addOrder(){
        orderService.saveOrder();
    }

    @ApiOperation("按照keyId 查询订单")
    @RequestMapping(value = "/order/{keyId}", method = RequestMethod.GET)
    public String getOrderId(@PathVariable Integer keyId){
        return orderService.getOrderId(keyId);
    }

}
