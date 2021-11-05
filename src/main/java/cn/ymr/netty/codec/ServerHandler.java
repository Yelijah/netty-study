package cn.ymr.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-04
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server accept mgs:" + ((ByteBuf) msg).toString(StandardCharsets.UTF_8));
    }
}