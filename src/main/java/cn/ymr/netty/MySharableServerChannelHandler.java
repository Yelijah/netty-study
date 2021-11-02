package cn.ymr.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Desc
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
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        activeCounts--;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        receivedCounts++;
    }

    public int getReceivedCounts() {
        return receivedCounts;
    }

    public int getActiveCounts() {
        return activeCounts;
    }
}