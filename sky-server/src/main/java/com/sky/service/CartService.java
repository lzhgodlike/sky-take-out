package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;

import java.util.List;

public interface CartService {
    List<ShoppingCart> list();

    Result add(ShoppingCartDTO shoppingCartDTO);

    Result sub(ShoppingCartDTO shoppingCartDTO);

    Result clean();
}
