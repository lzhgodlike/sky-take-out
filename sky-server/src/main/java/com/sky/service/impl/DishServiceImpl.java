package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品及口味
     *
     * @param dishDTO
     * @return
     */
    @Override
    @ApiOperation("新增菜品")
    @Transactional(rollbackFor = Exception.class)
    public Result save(DishDTO dishDTO) {
        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishMapper.save(dishDTO);
        // 只有当 flavors 不为空时才调用 saveFlavors
        if (flavors != null && !flavors.isEmpty()) {
            dishMapper.saveFlavors(dishDTO.getId(), flavors);
        }
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishVOPage = (Page<DishVO>) dishMapper.pageQuery(dishPageQueryDTO);
        PageResult pageResult = new PageResult(dishVOPage.getTotal(), dishVOPage.getResult());
        return Result.success(pageResult);
    }

    /**
     * 修改菜品及口味
     *
     * @param dishDTO
     * @return
     */
    @Override
    @ApiOperation("修改菜品")
    @Transactional(rollbackFor = Exception.class)
    public Result update(DishDTO dishDTO) {
        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishMapper.update(dishDTO);
        if (flavors != null && !flavors.isEmpty()) {
            dishMapper.deleteFlavors(dishDTO.getId());
            dishMapper.saveFlavors(dishDTO.getId(), flavors);
        }
        return Result.success();
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Override
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(Long id) {
        DishVO dishVO = dishMapper.getById(id);
        dishVO.setFlavors(dishMapper.getFlavorsByDishId(id));
        return Result.success(dishVO);
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Override
    public Result<List<DishDTO>> list(Long categoryId) {
        List<DishDTO> list = dishMapper.list(categoryId);
        return Result.success(list);
    }

    /**
     * 菜品起售、停售
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Result dishStatus(Integer status, Long id) {
        DishDTO dishDTO = new DishDTO();
        dishDTO.setStatus(status);
        dishDTO.setId(id);
        dishMapper.update(dishDTO);
        return Result.success();
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @Override
    public Result delete(List<Long> ids) {
        dishMapper.delete(ids);
        return Result.success();
    }
}
