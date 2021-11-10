package cn.ymr.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.charset.StandardCharsets;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-09
 */
public class MyServerOutChannelHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("服务器开始发送数据:" + ((ByteBuf)msg).toString(StandardCharsets.UTF_8));
        promise.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("服务器发送数据成功!");
            } else {
                future.cause().printStackTrace();
                System.out.println("服务器发送数据失败!");
            }
        });
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务器开始flush数据");
        super.flush(ctx);
    }
}