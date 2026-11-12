package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
@Tag(name = "套餐管理", description = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     *
     * @param setmealDTO 套餐信息
     * @return Result<Void>
     */
    @PostMapping
    @Operation(summary = "新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result<Void> save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO 分页查询条件
     * @return Result<PageResult>
     */
    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id列表
     * @return Result<Void>
     */
    @DeleteMapping
    @Operation(summary = "批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<Void> deleteByIds(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        setmealService.deleteByIds(ids);
        return Result.success();
    }

    /**
     * 套餐起售、停售
     *
     * @param status 套餐状态，1表示起售，0表示停售
     * @param id     套餐id
     * @return Result<Void>
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "套餐起售停售")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<Void> startOrStop(@PathVariable("status") Integer status, Long id) {
        log.info("套餐起售停售：{}, {}", status, id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据id查询套餐
     *
     * @param id 套餐id
     * @return Result<SetmealVO>
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable("id") Long id) {
        log.info("根据id查询套餐：{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO 套餐信息
     * @return Result<Void>
     */
    @PutMapping
    @Operation(summary = "修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<Void> update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }
}
