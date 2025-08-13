package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartMapper {

    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> list(Long userId);

    void add(ShoppingCart cart);

    ShoppingCart select(ShoppingCart shoppingCart);

    void update(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where id = #{id}")
    void delete(Long id);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clean(Long userId);
}
