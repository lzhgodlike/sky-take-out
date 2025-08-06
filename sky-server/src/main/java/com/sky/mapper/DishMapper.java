package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品数据
     *
     * @param dishDTO
     */
    @AutoFill(value = OperationType.INSERT)
    void save(DishDTO dishDTO);

    /**
     * 批量保存菜品口味数据
     *
     * @param dishId
     * @param flavors
     */
    void saveFlavors(@Param("dishId") Long dishId, @Param("flavors") List<DishFlavor> flavors);
}
