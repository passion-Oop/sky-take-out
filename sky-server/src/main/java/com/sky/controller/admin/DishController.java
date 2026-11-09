package com.sky.controller.admin;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import com.sky.vo.DishWithSetmealItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
@Tag(name = "菜品管理", description = "包含菜品的增删改查")
public class DishController {

    @Autowired
    DishService dishService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private RedisCacheManager cacheManager;

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
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<Void> deleteByIds(@RequestParam List<Long> ids){
        log.info("批量删除菜品: {}", ids);
        dishService.deleteByIds(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启用禁用菜品")
    public Result<Void> startOrShop(@PathVariable Integer status, Long id){
        log.info("启用禁用菜品：{}, {}", status, id);

        dishService.startAndShop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品: {}", id);   
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @Operation(summary = "编辑菜品")
    public Result<Void> update(@RequestBody DishDTO dishDTO){
        log.info("编辑菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据categoryId查询菜品")
    public Result<List<DishWithSetmealItemVO>> getByCategoryId(Long categoryId){
        log.info("根据categoryId查询菜品: {}", categoryId);
        List<DishWithSetmealItemVO> dishWithSetmealItemVOList = dishService.getByCategoryId(categoryId);
        return Result.success(dishWithSetmealItemVOList);
    }

}
