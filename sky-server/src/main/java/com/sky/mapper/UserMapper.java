package com.sky.mapper;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    UserLoginVO select(UserLoginDTO userLoginDTO);

    Integer insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    List<Map<String, Object>> getByBeginAndEndTime(@Param("begin") LocalDate begin, @Param("end") LocalDate end);

    @Select("select count(id) from user where create_time < #{begin}")
    int getTotalUntilDateByDate(LocalDate begin);
}
