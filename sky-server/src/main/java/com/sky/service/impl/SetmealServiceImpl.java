package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result save(SetmealDTO setmealDTO) {
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);
        // 只有当 setmealDishes 不为空时才调用 saveSetmealDishes
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(dish -> dish.setSetmealId(setmeal.getId()));
            setmealMapper.saveSetmealDishes(setmeal.getId(), setmealDishes);
        }
        return Result.success();
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> setmealVOPage = (Page<SetmealVO>) setmealMapper.pageQuery(setmealPageQueryDTO);
        PageResult pageResult = new PageResult(setmealVOPage.getTotal(), setmealVOPage.getResult());
        return Result.success(pageResult);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        setmealMapper.deleteSetmealDishes(Collections.singletonList(setmealDTO.getId()));
        setmealMapper.saveSetmealDishes(setmeal.getId(), setmealDTO.getSetmealDishes());
        return Result.success();
    }

    /**
     * 根据id查询套餐和套餐菜品关系
     *
     * @param id
     * @return
     */
    @Override
    public Result<SetmealVO> getById(Long id) {
        SetmealVO setmealVO = setmealMapper.getById(id);
        setmealVO.setSetmealDishes(setmealMapper.getDishesById(id));
        return Result.success(setmealVO);
    }

    /**
     * 起售、停售套餐
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Result setmealStatus(Integer status, Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
        return Result.success();
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delete(List<Long> ids) {
        setmealMapper.delete(ids);
        setmealMapper.deleteSetmealDishes(ids);
        return Result.success();
    }
}
