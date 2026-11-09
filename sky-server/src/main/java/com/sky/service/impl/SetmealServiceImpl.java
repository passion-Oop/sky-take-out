package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.Collections;
import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     *
     * @param setmealDTO 套餐信息
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {

        // 保存套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        // 保存套餐和菜品的关联关系
        saveSetmealDish(setmeal.getId(), setmealDTO.getSetmealDishes());
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id批量删除套餐
     *
     * @param ids 套餐id列表
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        // 起售中的套餐不能删除
        List<Setmeal> setmealList = setmealMapper.getByIds(ids);
        boolean onSale = setmealList.stream()
                .anyMatch(setmeal -> StatusConstant.ENABLE.equals(setmeal.getStatus()));

        if (onSale) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        // 删除套餐和套餐菜品关联关系
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 套餐起售、停售
     *
     * @param status 套餐状态，1表示起售，0表示停售
     * @param id     套餐id
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        // 起售时校验套餐内菜品是否全部起售
        if (StatusConstant.ENABLE.equals(status)) {
            List<Long> dishIds = setmealDishMapper.getDishIdsBySetmealId(id);

            if (CollectionUtils.isEmpty(dishIds)) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }

            boolean hasDisabledDish = dishMapper.getByIds(dishIds).stream()
                    .anyMatch(dish -> !StatusConstant.ENABLE.equals(dish.getStatus()));

            if (hasDisabledDish) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }

        setmealMapper.update(Setmeal.builder()
                .id(id)
                .status(status)
                .build());
    }

    /**
     * 根据id查询套餐，同时带出套餐包含的菜品
     *
     * @param id 套餐id
     * @return 套餐信息
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {

        SetmealVO setmealVO = setmealMapper.getByIdWithCategory(id);

        if (setmealVO == null) {
            return null;
        }

        setmealVO.setSetmealDishes(setmealDishMapper.getBySetmealId(id));
        return setmealVO;
    }

    /**
     * 修改套餐，同时更新套餐和菜品的关联关系
     *
     * @param setmealDTO 套餐信息
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {

        // 修改套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 先删除原有的关联关系，再保存新的关联关系
        setmealDishMapper.deleteBySetmealIds(Collections.singletonList(setmealDTO.getId()));
        saveSetmealDish(setmealDTO.getId(), setmealDTO.getSetmealDishes());
    }

    /**
     * 保存套餐和菜品的关联关系
     *
     * @param setmealId     套餐id
     * @param setmealDishes 套餐菜品关系列表
     */
    private void saveSetmealDish(Long setmealId, List<SetmealDish> setmealDishes) {

        if (CollectionUtils.isEmpty(setmealDishes)) {
            return;
        }

        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
        setmealDishMapper.insertBatch(setmealDishes);
    }
}
