package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnRecvMain;

public interface ConnRecvMainDao {
    int deleteByPrimaryKey(String id);

    int insert(ConnRecvMain record);

    int insertSelective(ConnRecvMain record);

    ConnRecvMain selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ConnRecvMain record);

    int updateByPrimaryKey(ConnRecvMain record);
}