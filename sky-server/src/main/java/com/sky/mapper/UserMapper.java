package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import com.sky.vo.UserLoginVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    UserLoginVO select(UserLoginDTO userLoginDTO);

    Integer insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long userId);
}
