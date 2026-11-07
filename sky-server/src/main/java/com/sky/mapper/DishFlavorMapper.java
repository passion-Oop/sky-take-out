package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    public void insertBatch(List<DishFlavor> flavors);

    void deleteByDishId(List<Long> ids);

    @Select("select * from sky_take_out.dish_flavor where dish_id = #{id}")
    List<DishFlavor> getById(Long id);

    /**
     * 根据菜品id批量查询口味
     *
     * @param dishIds 菜品id列表
     * @return 口味列表
     */
    List<DishFlavor> getByDishIds(List<Long> dishIds);
}
