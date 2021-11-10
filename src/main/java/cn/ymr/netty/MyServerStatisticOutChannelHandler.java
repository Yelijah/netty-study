package cn.ymr.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-10
 */
@ChannelHandler.Sharable
public class MyServerStatisticOutChannelHandler extends ChannelOutboundHandlerAdapter {
    private int writeCount = 0;
    private int flushCount = 0;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        writeCount++;
        System.out.println("writeCount:" + writeCount);
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        flushCount++;
        System.out.println("flushCount:" + flushCount);
        super.flush(ctx);
    }
}