package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnTotalNum;

public interface ConnTotalNumDao {
    int deleteByPrimaryKey(String id);

    int insert(ConnTotalNum record);

    int insertSelective(ConnTotalNum record);

    ConnTotalNum selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ConnTotalNum record);

    int updateByPrimaryKey(ConnTotalNum record);

    int updateTotalNum(String type);
}