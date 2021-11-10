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
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("服务器接收数据:" + buf.toString(StandardCharsets.UTF_8));
        /**
         * 1.flush会释放byteBuf，所以引用计数器为0
         * 2.ctx.write会从所在handler向前找最近的OutboundHandler，在次handler后的不会触发； 而channel.write会从最后的OutboundHandler一直往前触发
         */
        ctx.writeAndFlush(buf);
        assert buf.refCnt() == 0;
        //触发下个Handler执行，类似于filterChain.doFilter
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务器读取消息完毕！");
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("服务器已收到消息，向你回执\n", StandardCharsets.UTF_8));
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}