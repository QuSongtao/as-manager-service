package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnRecvMsg;

public interface ConnRecvMsgDao {
    int deleteByPrimaryKey(String id);

    int insert(ConnRecvMsg record);

    int insertSelective(ConnRecvMsg record);

    ConnRecvMsg selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ConnRecvMsg record);

    int updateByPrimaryKeyWithBLOBs(ConnRecvMsg record);

    int updateByPrimaryKey(ConnRecvMsg record);
}