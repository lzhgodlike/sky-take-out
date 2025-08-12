package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
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

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     */
    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 更新菜品
     *
     * @param dishDTO
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(DishDTO dishDTO);

    /**
     * 批量更新菜品口味
     *
     * @param id
     */
    void deleteFlavors(Long id);

    /**
     * 根据id查询菜品数据
     *
     * @param id
     * @return
     */
    DishVO getById(Long id);

    /**
     * 根据菜品id查询菜品口味数据
     *
     * @param id
     * @return
     */
    List<DishFlavor> getFlavorsByDishId(Long id);

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    List<Dish> list(Long categoryId);

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    void delete(List<Long> ids);

    List<DishVO> getByIds(List<Long> ids);
}
