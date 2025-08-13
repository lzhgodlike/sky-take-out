package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/shoppingCart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("list")
    public Result<List<ShoppingCart>> list() {
        return Result.success(cartService.list());
    }

    @PostMapping("add")
    public Result add(@RequestBody ShoppingCart shoppingCart) {
        log.info("添加购物车：{}", shoppingCart);
        return cartService.add(shoppingCart);
    }

    @PostMapping("sub")
    public Result sub(@RequestBody ShoppingCart shoppingCart) {
        return cartService.sub(shoppingCart);
    }

    @DeleteMapping("clean")
    public Result clean() {
        return cartService.clean();
    }
}
