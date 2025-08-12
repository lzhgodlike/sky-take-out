package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private RedisTemplate redisTemplate;

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
        String key = "dish_" + dishDTO.getCategoryId();
        log.info("保存菜品信息，清理缓存数据：" + key);
        redisTemplate.delete(key);
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
        DishVO dishVO = dishMapper.getById(dishDTO.getId());
        dishMapper.update(dishDTO);
        if (flavors != null && !flavors.isEmpty()) {
            dishMapper.deleteFlavors(dishDTO.getId());
            dishMapper.saveFlavors(dishDTO.getId(), flavors);
        }
        String key = "dish_" + dishDTO.getCategoryId();
        if (dishVO != null && !dishVO.getCategoryId().equals(dishDTO.getCategoryId())) {
            log.info("修改菜品信息，清理缓存数据：" + key);
            redisTemplate.delete(key);
            key = "dish_" + dishVO.getCategoryId();
        }
        log.info("修改菜品信息，清理缓存数据：" + key);
        redisTemplate.delete(key);
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
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishMapper.list(categoryId);
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
        DishVO dishVO = dishMapper.getById(id);
        String key = "dish_" + dishVO.getCategoryId();
        dishDTO.setStatus(status);
        dishDTO.setId(id);
        dishMapper.update(dishDTO);
        log.info("修改菜品信息，清理缓存数据：" + key);
        redisTemplate.delete(key);
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
        List<DishVO> dishVOList = dishMapper.getByIds(ids);
        dishMapper.delete(ids);
        dishVOList.stream()
                .map(DishVO::getCategoryId)
                .distinct() // 去重，避免重复删除同一个分类的缓存
                .forEach(categoryId -> {
                    String key = "dish_" + categoryId;
                    log.info("删除缓存key:{}", key);
                    redisTemplate.delete(key);
                });
        return Result.success();
    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish.getCategoryId());

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishMapper.getFlavorsByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

}
