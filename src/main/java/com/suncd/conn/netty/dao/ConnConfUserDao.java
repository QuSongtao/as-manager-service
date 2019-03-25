package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnConfUser;

public interface ConnConfUserDao {
    int deleteByPrimaryKey(Integer id);

    int insert(ConnConfUser record);

    int insertSelective(ConnConfUser record);

    ConnConfUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ConnConfUser record);

    int updateByPrimaryKey(ConnConfUser record);
}