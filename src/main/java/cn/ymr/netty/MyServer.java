package cn.ymr.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-02
 */
public class MyServer {

    public static void main(String[] args) throws Exception {
        int port = args.length == 0 ? 9999 : Integer.parseInt(args[0]);
        new MyServer(port).start();
    }

    private final int port;

    public MyServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        ChannelHandler statisticChannelHandler = createStatisticsChannelHandler();
        ChannelHandler statisticOutHandler = new MyServerStatisticOutChannelHandler();
        //自定义业务线程池loopGroup
        EventLoopGroup busyTaskGroup = new DefaultEventLoopGroup();
        try{
            AtomicReference<ChannelPipeline> pipelineRef = new AtomicReference<>();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println(socketChannel.remoteAddress() + " channel init!");
                            assert pipelineRef.get() != socketChannel.pipeline();

                            pipelineRef.set(socketChannel.pipeline());
                            socketChannel.pipeline()
                                    //write所在ChannelHandler（in or out）之前的OutChannelHandler才能拦截到write事件
                                    .addLast(new MyServerOutChannelHandler())
                                    .addLast(new MyServerChannelHandler())
                                    .addLast(busyTaskGroup, new MyServerBusyChannelHandler(5))
                                    .addLast(statisticChannelHandler)
                                    .addLast(statisticOutHandler);
                        }
                    });
            ChannelFuture future = bootstrap.bind().sync(); //sync阻塞线程，直到启动完成，否则是异步非阻塞的
            System.out.println("服务器启动完成~");
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }


    private static ChannelHandler createStatisticsChannelHandler() {
        MySharableServerChannelHandler sharableChannelHandler = new MySharableServerChannelHandler();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(()-> {
            System.out.println(sharableChannelHandler.getActiveCounts() + " " + sharableChannelHandler.getReceivedCounts());
        }, 5, 5, TimeUnit.SECONDS);
        return sharableChannelHandler;
    }
}