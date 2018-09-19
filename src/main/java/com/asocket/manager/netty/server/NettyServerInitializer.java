package com.asocket.manager.netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 服务端通道初始化
 *
 * @author qust
 * @version 1.0 20180918
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 初始化channel
     * <p>
     * 注: 通信底层采用字节传递,因此在此无需进行编码与解码
     *
     * @author qust
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 配置服务端消息处理器
        pipeline.addLast(new NettyServerHandler());
    }
}
