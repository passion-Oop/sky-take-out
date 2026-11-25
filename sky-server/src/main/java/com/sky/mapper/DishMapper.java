package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import com.sky.vo.DishWithSetmealItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);


    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    List<Dish> getByIds(List<Long> ids);

    void deleteById(List<Long> ids);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish build);

    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据条件查询菜品
     *
     * @param dish 查询条件（分类id、状态、名称）
     * @return 菜品列表
     */
    List<Dish> list(Dish dish);

    @Select("select * from dish where category_id = #{categoryId}")
    List<DishWithSetmealItemVO> getByCategoryId(Long categoryId);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
