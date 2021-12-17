package cn.ymr.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;


/**
 * Desc
 *
 * @author Elijah
 * created on 2021-12-17
 */
public class MyClientChannelOutHandler extends ChannelOutboundHandlerAdapter {
    private Logger logger = Logger.getLogger(MyClientChannelOutHandler.class.getSimpleName());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        logger.info("get msg:" + ((ByteBuf)msg).toString(StandardCharsets.UTF_8));
        Thread.sleep(3000);
        logger.info("out msg:" + ((ByteBuf)msg).toString(StandardCharsets.UTF_8));
        super.write(ctx, msg, promise);
    }
}