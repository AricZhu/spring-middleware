package com.aric.middleware;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.mybatis.Resource;
import com.aric.middleware.mybatis.SqlSession;
import com.aric.middleware.mybatis.SqlSessionFactory;
import com.aric.middleware.mybatis.SqlSessionFactoryBuilder;
import com.aric.middleware.mybatis.dto.UserQueryDTO;
import com.aric.middleware.mybatis.po.User;

import java.io.Reader;

public class MybatisDemo {
    public static void main(String[] args) throws Exception {
        Reader reader = Resource.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        // 参数修改
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(null, "");
        SqlSession sqlSession = sqlSessionFactory.openSession();

        UserQueryDTO userQueryDTO = new UserQueryDTO();
        userQueryDTO.setUserId("980765512");
        userQueryDTO.setUserNickName("铁锤");

        User user = (User)sqlSession.selectOne("com.aric.middleware.mybatis.dao.IUserDao.queryUserInfoByUserIdAndNickname", userQueryDTO);

        System.out.println(JSON.toJSONString(user));

        reader.close();
        sqlSession.close();
    }
}
