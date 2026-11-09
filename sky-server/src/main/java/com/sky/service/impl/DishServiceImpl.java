package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import com.sky.vo.DishWithSetmealItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Autowired
    SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     *
     * @param dishDTO 菜品信息
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        // 保存菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        // 保存菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO 菜品分页查询条件
     * @return 菜品分页查询结果
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        // 分页查询菜品
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());

    }

    /**
     * 根据id删除菜品
     *
     * @param ids 菜品id列表
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {

        // 查询菜品是否在售
        List<Dish> dishList = dishMapper.getByIds(ids)
                .stream()
                .filter(dish -> dish.getStatus() == 1)
                .toList();

        // 如果在售，抛出业务异常
        if (!CollectionUtils.isEmpty(dishList)) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        // 查询菜品是否被套餐关联
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);

        if (!CollectionUtils.isEmpty(setmealIdsByDishIds)) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品和口味
        dishMapper.deleteById(ids);
        dishFlavorMapper.deleteByDishId(ids);

    }

    @Override
    public void startAndShop(Integer status, Long id) {
        dishMapper.update(Dish.builder()
                .status(status)
                .id(id)
                .build());
    }

    @Override
    @Transactional
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavorList = dishFlavorMapper.getById(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavorList);

        return dishVO;
    }

    /**
     * 根据id更新菜品和口味
     *
     * @param dishDTO 菜品信息
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.update(dish);

        dishFlavorMapper.deleteByDishId(Collections.singletonList(dishDTO.getId()));

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (!CollectionUtils.isEmpty(flavors)) {

            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);

        }
    }

    /**
     * 根据条件查询菜品，并带出菜品对应的口味
     *
     * @param dish 查询条件（分类id、状态、名称）
     * @return 菜品及口味列表
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {

        List<Dish> dishList = dishMapper.list(dish);

        if (CollectionUtils.isEmpty(dishList)) {
            return Collections.emptyList();
        }

        List<Long> dishIds = dishList.stream()
                .map(Dish::getId)
                .toList();

        Map<Long, List<DishFlavor>> flavorMap = dishFlavorMapper.getByDishIds(dishIds)
                .stream()
                .collect(Collectors.groupingBy(DishFlavor::getDishId));
        
        List<DishVO> dishVOList = new ArrayList<>(dishList.size());

        for (Dish d : dishList) {

            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            dishVO.setFlavors(flavorMap.getOrDefault(d.getId(), new ArrayList<>()));
            
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

    @Override
    public List<DishWithSetmealItemVO> getByCategoryId(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }

}