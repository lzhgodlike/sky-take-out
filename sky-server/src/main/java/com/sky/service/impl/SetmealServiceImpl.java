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
import org.springframework.data.redis.core.RedisTemplate;
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
    @Autowired
    private RedisTemplate redisTemplate;

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
        String key = "setmeal_list_" + setmealDTO.getCategoryId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.save(setmeal);
        // 只有当 setmealDishes 不为空时才调用 saveSetmealDishes
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            System.out.println("id:" + setmealDTO.getId());
            setmealMapper.saveSetmealDishes(setmeal.getId(), setmealDishes);
        }
        redisTemplate.delete(key);
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
        String key = "setmeal_list_" + setmealDTO.getCategoryId();
        SetmealVO setmealVO = setmealMapper.getById(setmealDTO.getId());
        if (setmealVO != null && !setmealVO.getCategoryId().equals(setmealDTO.getCategoryId())) {
            log.info("修改套餐分类，清理缓存数据：" + key);
            redisTemplate.delete(key);
            key = "setmeal_list_" + setmealVO.getCategoryId();
        }
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        setmealMapper.deleteSetmealDishes(Collections.singletonList(setmealDTO.getId()));
        setmealMapper.saveSetmealDishes(setmealDTO.getId(), setmealDTO.getSetmealDishes());
        log.info("修改套餐信息，清理缓存数据：" + key);
        redisTemplate.delete(key);
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
        SetmealVO setmealVO = setmealMapper.getById(id);
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
        String key = "setmeal_list_" + setmealVO.getCategoryId();
        log.info("删除缓存key:{}", key);
        redisTemplate.delete(key);
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
        List<Long> categoryIds = null;
        for (Long id : ids) {
            SetmealVO setmealVO = setmealMapper.getById(id);
            // 把套餐的分类id保存起来,等待所有套餐删除完毕再删除缓存
            categoryIds = Collections.singletonList(setmealVO.getCategoryId());
        }
        setmealMapper.deleteSetmealDishes(ids);
        setmealMapper.delete(ids);
        for (Long categoryId : categoryIds) {
            String key = "setmeal_list_" + categoryId;
            log.info("删除缓存key:{}", key);
            redisTemplate.delete(key);
        }
        return Result.success();
    }
}
