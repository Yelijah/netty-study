package cn.ymr.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-02
 */
public class MyServerChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(this);
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("server received:" + buf.toString(StandardCharsets.UTF_8));
        ctx.writeAndFlush(buf);
        //触发下个Handler执行，类似于filterChain.doFilter
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("服务器已收到消息，向你回执\n", StandardCharsets.UTF_8));
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}