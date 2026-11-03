package com.sky.controller.admin;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
@Tag(name = "菜品管理", description = "包含菜品的增删改查")
public class DishController {

    @Autowired
    DishService dishService;

    @PostMapping
    @Operation(summary = "新增菜品")
    public Result<Void> save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品: {}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(new PageResult(pageResult.getTotal(), pageResult.getRecords()));
    }

    @DeleteMapping
    @Operation(summary = "批量删除菜品")
    public Result<Void> deleteByIds(@RequestParam List<Long> ids){
        log.info("批量删除菜品: {}", ids);
        dishService.deleteByIds(ids);
        return Result.success();
    }
}
