package com.aric.middleware.dbrouter.mapper;

import com.aric.middleware.dbrouter.annotation.DBRouterAnnotation;
import com.aric.middleware.dbrouter.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    @DBRouterAnnotation(key = "userId")
    void insertInto(User user);

    @DBRouterAnnotation(key = "userId")
    User queryUser(User user);
}
