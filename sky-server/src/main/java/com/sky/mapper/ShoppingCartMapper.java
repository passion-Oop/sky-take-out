package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    // 注意：MyBatis 的 statement id 只按方法名生成，不能对同名方法做重载，
    // 否则会和 XML 中的 <select id="list"> 冲突
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    // 购物车表没有create_user/update_user字段，且属于C端用户数据，因此不使用@AutoFill
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    void updateNumberById(ShoppingCart shoppingCart);
}
