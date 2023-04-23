package com.example.lock.controller;


import com.example.lock.service.InventoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "redis 分布式锁测试")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @ApiOperation("扣除库存，一次卖一个")
    @GetMapping("/inventory/sale")
    public String sale() {
        return inventoryService.sale();
    }

}
