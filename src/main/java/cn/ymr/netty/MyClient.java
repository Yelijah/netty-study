package cn.ymr.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-02
 */
public class MyClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("192.168.250.3", 9999))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new MyClientChannelOutHandler());
                            socketChannel.pipeline().addLast(new MyClientChannelHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            System.out.println("客户端开启完成~");

            //指定再多线程也没用，一个channel始终只有一个线程处理
            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                int c = i + 'a';
                executor.submit(() -> {
                    ByteBuf buf = Unpooled.buffer();
                    for (int j = 0; j < 100; j++) {
                        buf.writeByte(c);
                    }
                    buf.writeByte('\n');
                    //不会有交叉字符出现
                    //一个channel始终只有一个线程处理，所以后续的消息全都被堆积在队列中，等待线程按顺序处理
                    channelFuture.channel().writeAndFlush(buf);
                });
            }
            executor.shutdown();
            Thread.sleep(15000);
            channelFuture.channel().close();
        } finally {
            group.shutdownGracefully();
        }
    }
}