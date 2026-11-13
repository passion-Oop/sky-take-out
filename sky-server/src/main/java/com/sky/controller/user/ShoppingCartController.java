package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Tag(name = "购物车相关", description = "购物车相关接口")
public class ShoppingCartController {

    @Autowired
    ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @Operation(summary = "添加购物车相关接口")
    public Result<Void> addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车数据：{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取购物车信息相关接口")
    public Result<List<ShoppingCart>> getShoppingCartDTO() {
        log.info("获取购物车数据");
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

    @PostMapping("/sub")
    @Operation(summary = "购物车减量相关接口")
    public Result<Void> subShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车减量数据：{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @Operation(summary = "清空购物车相关接口")
    public Result<Void> cleanShoppingCart() {
        log.info("清空{}用户购物车数据", BaseContext.getCurrentId());
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }


}
