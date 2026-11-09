package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import java.util.List;

public interface SetmealService {

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     *
     * @param setmealDTO 套餐信息
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id批量删除套餐
     *
     * @param ids 套餐id列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 套餐起售、停售
     *
     * @param status 套餐状态，1表示起售，0表示停售
     * @param id     套餐id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询套餐，同时带出套餐包含的菜品
     *
     * @param id 套餐id
     * @return 套餐信息
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐，同时更新套餐和菜品的关联关系
     *
     * @param setmealDTO 套餐信息
     */
    void updateWithDish(SetmealDTO setmealDTO);

}
