package cn.ymr.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-02
 */
public class MyClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        System.out.println("收到服务器消息:" + byteBuf.toString(StandardCharsets.UTF_8));

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("sssss~", CharsetUtil.UTF_8));
    }
}