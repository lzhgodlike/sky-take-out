package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    Result save(DishDTO dishDTO);

    Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO);

    Result update(DishDTO dishDTO);

    Result<DishVO> getById(Long id);

    Result<List<Dish>> list(Long categoryId);

    Result dishStatus(Integer status, Long id);

    Result delete(List<Long> ids);

    List<DishVO> listWithFlavor(Dish dish);
}
