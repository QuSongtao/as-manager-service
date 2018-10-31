package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnSendMain;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConnSendMainDao {
    int deleteByPrimaryKey(String id);

    ConnSendMain selectByTimeAndSeq(@Param("pushTime") int pushTime, @Param("seqNo") int seqNo);

    int insert(ConnSendMain record);

    int insertSelective(ConnSendMain record);

    ConnSendMain selectByPrimaryKey(String id);

    List<ConnSendMain> selectBySendFlag(@Param("sendFlag") String sendFlag, @Param("telType") String telType);

    int updateByPrimaryKeySelective(ConnSendMain record);

    int updateByPrimaryKey(ConnSendMain record);
}