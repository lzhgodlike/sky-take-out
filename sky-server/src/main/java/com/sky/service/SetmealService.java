package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

    Result save(SetmealDTO setmealDTO);

    Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO);

    Result update(SetmealDTO setmealDTO);

    Result<SetmealVO> getById(Long id);

    Result setmealStatus(Integer status, Long id);

    Result delete(List<Long> ids);
}
