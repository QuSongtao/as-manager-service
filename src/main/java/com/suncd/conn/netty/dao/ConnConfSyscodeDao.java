package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnConfSyscode;
import org.apache.ibatis.annotations.Param;

public interface ConnConfSyscodeDao {
    int deleteByPrimaryKey(String id);

    int insert(ConnConfSyscode record);

    int insertSelective(ConnConfSyscode record);

    ConnConfSyscode selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ConnConfSyscode record);

    int updateByPrimaryKey(ConnConfSyscode record);

    ConnConfSyscode selectBySysCode(@Param("sysCode") String sysCode);
}