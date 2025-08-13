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
    public Result add(ShoppingCart shoppingCart) {
        log.info("添加购物车：{}", shoppingCart);
        Long userId = BaseContext.getCurrentId();
        if (shoppingCart.getDishId() != null) {
            // 获取菜品的信息，然后添加进cart
            shoppingCart.setUserId(userId);
            ShoppingCart shoppingCart1 = cartMapper.select(shoppingCart);
            if (shoppingCart1 == null) {
                log.info("购物车不存在该菜品");
                Result<DishVO> dishVOResult = dishService.getById(shoppingCart.getDishId());
                ShoppingCart cart = ShoppingCart.builder()
                        // 获取当前用户id
                        .userId(userId)
                        .dishId(shoppingCart.getDishId())
                        .number(1)
                        .amount(dishVOResult.getData().getPrice())
                        .image(dishVOResult.getData().getImage())
                        .name(dishVOResult.getData().getName())
                        .createTime(LocalDateTime.now())
                        .build();
                if (shoppingCart.getDishFlavor() != null)
                    cart.setDishFlavor(shoppingCart.getDishFlavor());
                cartMapper.add(cart);
                return Result.success();
            } else {
                log.info("购物车已存在该菜品{}", shoppingCart1);
                ShoppingCart cart = new ShoppingCart();
                cart.setNumber(shoppingCart1.getNumber() + 1);
                cart.setId(shoppingCart1.getId());
                cartMapper.update(cart);
                return Result.success();
            }
        } else if (shoppingCart.getSetmealId() != null) {
            // 获取套餐的信息，然后添加进cart
            shoppingCart.setUserId(userId);
            ShoppingCart shoppingCart1 = cartMapper.select(shoppingCart);
            if (shoppingCart1 == null) {
                SetmealVO setmealVO = setmealService.getById(shoppingCart.getSetmealId()).getData();
                ShoppingCart cart = ShoppingCart.builder()
                        .userId(userId)
                        .setmealId(shoppingCart.getSetmealId())
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
                cart.setNumber(shoppingCart1.getNumber() + 1);
                cart.setId(shoppingCart1.getId());
                cartMapper.update(cart);
                return Result.success();
            }
        }
        return Result.error("添加失败");
    }

    @Override
    public Result sub(ShoppingCart shoppingCart) {
        log.info("删除购物车：{}", shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        ShoppingCart cart1 = cartMapper.select(shoppingCart);
        if (cart1.getNumber() == 1) {
            cartMapper.delete(cart1.getId());
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setNumber(cart1.getNumber() - 1);
            cart.setId(cart1.getId());
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
