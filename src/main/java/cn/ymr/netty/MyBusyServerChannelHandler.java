package cn.ymr.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-03
 */
public class MyBusyServerChannelHandler extends ChannelInboundHandlerAdapter {
    private int second = 1;

    public MyBusyServerChannelHandler(int second) {
        this.second = second;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " begin busy task~" + second);
        Thread.sleep(second * 1000L);
        System.out.println(ctx.channel().remoteAddress() + " end busy task~" + second);
        //虽然自定义业务线程池LoopGroup处理耗时任务，能够避免同一个NioEventLoop中的其他Channel在较长的时间内都无法得到处理
        //但是避免不了阻塞当前channel的下个Handler，因为得等耗时的Handler触发context#fire。
        ctx.fireChannelRead(msg);
    }
}