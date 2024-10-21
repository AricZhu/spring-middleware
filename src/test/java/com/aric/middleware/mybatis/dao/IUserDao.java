package com.aric.middleware.mybatis.dao;

import com.aric.middleware.mybatis.po.User;

public interface IUserDao {
    User queryUserInfoById(Long id);

    User queryUserList(String nickName);
}
