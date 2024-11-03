package com.aric.middleware.mybatis.dao;

import com.aric.middleware.mybatis.dto.UserQueryDTO;
import com.aric.middleware.mybatis.po.User;

public interface IUserDao {
    User queryUserInfoById(Long id);

    User queryUserInfoByUserIdAndNickname(UserQueryDTO userQueryDTO);

    User queryUserList(String nickName);
}
