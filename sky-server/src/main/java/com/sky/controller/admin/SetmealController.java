package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "套餐相关接口")
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
//    @CachePut(value = "setmealCache", key = "#setmealDTO.id")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        return setmealService.save(setmealDTO);
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        return setmealService.page(setmealPageQueryDTO);
    }

    /**
     * 套餐修改
     *
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("套餐修改")
    @Caching(evict = {
            @CacheEvict(value = "setmealCache", key = "#setmealDTO.categoryId"),
            @CacheEvict(value = "setmealDishCache", key = "#setmealDTO.id")
    })
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        return setmealService.update(setmealDTO);
    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return setmealService.getById(id);
    }

    /**
     * 起售、停售菜品
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售、停售套餐")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result dishStatus(@PathVariable Integer status, Long id) {
        return setmealService.setmealStatus(status, id);
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @Caching(evict = {
            @CacheEvict(value = "setmealCache", allEntries = true),
            @CacheEvict(value = "setmealDishCache", allEntries = true)
    })
    public Result delete(@RequestParam List<Long> ids) {
        return setmealService.delete(ids);
    }
}
