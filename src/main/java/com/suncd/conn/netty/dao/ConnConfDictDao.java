package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnConfDict;

public interface ConnConfDictDao {
    int deleteByPrimaryKey(String id);

    int insert(ConnConfDict record);

    int insertSelective(ConnConfDict record);

    ConnConfDict selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ConnConfDict record);

    int updateByPrimaryKey(ConnConfDict record);
}