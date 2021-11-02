package cn.ymr.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
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

    private int port;

    public MyServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        MySharableServerChannelHandler sharableChannelHandler = new MySharableServerChannelHandler();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(()-> {
            System.out.println(sharableChannelHandler.getActiveCounts() + " " + sharableChannelHandler.getReceivedCounts());
        }, 3, 3, TimeUnit.SECONDS);

        try{
            AtomicReference<ChannelPipeline> pipelineRef = new AtomicReference<>();
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println(socketChannel.remoteAddress());
                            if (pipelineRef.get() != null) {
                                //每个channel都有各自的pipeline
                                System.out.println(pipelineRef.get() == socketChannel.pipeline());
                            }
                            pipelineRef.set(socketChannel.pipeline());
                            socketChannel.pipeline().addLast(sharableChannelHandler);
                            socketChannel.pipeline().addLast(new MyServerChannelHandler());
                        }
                    });
            ChannelFuture future = b.bind().sync();
            System.out.println("服务器启动完成~");
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }


}