package cn.ymr.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 统计多少连接和消息.
 * 多channel共用一个channelHandler，必须加Sharable注解
 *
 * @author Elijah
 * created on 2021-11-02
 */
@ChannelHandler.Sharable
public class MySharableServerChannelHandler extends ChannelInboundHandlerAdapter {
    private int receivedCounts;
    private int activeCounts;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        activeCounts++;
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        activeCounts--;
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        receivedCounts++;
        //最后一个Handler如果触发fireChannelRead，则会在io.netty.channel.DefaultChannelPipeline.TailContext.channelRead中尝试释放ByteBuff;
        //此时如果之前已经释放了ByteBuff(比如触发了flush)，则会报错计数器异常:io.netty.util.IllegalReferenceCountException: refCnt: 0, decrement: 1
//        ctx.fireChannelRead((ByteBuf)msg);
    }

    public int getReceivedCounts() {
        return receivedCounts;
    }

    public int getActiveCounts() {
        return activeCounts;
    }
}