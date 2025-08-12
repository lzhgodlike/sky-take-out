package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.CartMapper;
import com.sky.result.Result;
import com.sky.service.CartService;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户id:{}查询购物车", userId);
        // 给list添加用户id
        return cartMapper.list(userId);
    }

    @Override
    public Result add(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车：{}", shoppingCartDTO);
        Long userId = BaseContext.getCurrentId();
        if (shoppingCartDTO.getDishId() != null) {
            // 获取菜品的信息，然后添加进cart
            ShoppingCart shoppingCart = cartMapper.select(shoppingCartDTO, userId);
            if (shoppingCart == null) {
                Result<DishVO> dishVOResult = dishService.getById(shoppingCartDTO.getDishId());
                ShoppingCart cart = ShoppingCart.builder()
                        // 获取当前用户id
                        .userId(userId)
                        .dishId(shoppingCartDTO.getDishId())
                        .number(1)
                        .amount(dishVOResult.getData().getPrice())
                        .image(dishVOResult.getData().getImage())
                        .name(dishVOResult.getData().getName())
                        .createTime(LocalDateTime.now())
                        .build();
                if (shoppingCartDTO.getDishFlavor() != null)
                    cart.setDishFlavor(shoppingCartDTO.getDishFlavor());
                cartMapper.add(cart);
                return Result.success();
            } else {
                ShoppingCart cart = new ShoppingCart();
                cart.setNumber(shoppingCart.getNumber() + 1);
                cart.setId(shoppingCart.getId());
                cartMapper.update(cart);
                return Result.success();
            }
        } else if (shoppingCartDTO.getSetmealId() != null) {
            // 获取套餐的信息，然后添加进cart
            ShoppingCart shoppingCart = cartMapper.select(shoppingCartDTO, userId);
            if (shoppingCart == null) {
                SetmealVO setmealVO = setmealService.getById(shoppingCartDTO.getSetmealId()).getData();
                ShoppingCart cart = ShoppingCart.builder()
                        .userId(userId)
                        .setmealId(shoppingCartDTO.getSetmealId())
                        .number(1)
                        .amount(setmealVO.getPrice())
                        .image(setmealVO.getImage())
                        .name(setmealVO.getName())
                        .createTime(LocalDateTime.now())
                        .build();
                cartMapper.add(cart);
                return Result.success();
            } else {
                ShoppingCart cart = new ShoppingCart();
                cart.setNumber(shoppingCart.getNumber() + 1);
                cart.setId(shoppingCart.getId());
                cartMapper.update(cart);
                return Result.success();
            }
        }
        return Result.error("添加失败");
    }

    @Override
    public Result sub(ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车：{}", shoppingCartDTO);
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = cartMapper.select(shoppingCartDTO, userId);
        if (shoppingCart.getNumber() == 1) {
            cartMapper.delete(shoppingCart.getId());
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setNumber(shoppingCart.getNumber() - 1);
            cart.setId(shoppingCart.getId());
            cartMapper.update(cart);
        }
        return Result.success();
    }

    @Override
    public Result clean() {
        log.info("清空购物车");
        cartMapper.clean(BaseContext.getCurrentId());
        return Result.success();
    }
}
