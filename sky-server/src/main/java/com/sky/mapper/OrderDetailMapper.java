package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    public void insertBatch(List<OrderDetail> list);


    List<OrderDetail> getByOrderIds(List<Long> orderIdList);

    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getById(Long id);
}
