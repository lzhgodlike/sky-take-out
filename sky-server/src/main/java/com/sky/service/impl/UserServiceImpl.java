package com.sky.service.impl;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import com.sky.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        UserLoginVO user = userMapper.select(userLoginDTO);
        if (user != null) {
            //登录，获取token
            log.info("用户已存在，执行登录");
        } else {
            //用户名不存在
            log.info("用户不存在，执行注册");
            User user1 = User.builder()
                    .openid(userLoginDTO.getCode())
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user1);
            user = userMapper.select(userLoginDTO);
        }
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);
        user.setToken(token);
        return user;
    }
}
