package com.suncd.conn.netty.dao;

import com.suncd.conn.netty.entity.ConnSendMain;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConnSendMainDao {
    int deleteByPrimaryKey(String id);

    ConnSendMain selectByTimeAndSeq(@Param("pushTime") int pushTime, @Param("seqNo") int seqNo);

    List<ConnSendMain> selectByReceiver(@Param("receiver") String receiver);

    int updateByPrimaryKeySelective(ConnSendMain record);
}