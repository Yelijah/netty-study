package cn.ymr.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-04
 */
public class CodecServer {
    public static void main(String[] args) throws Exception {
//        startServer(new ServerHandler()); // 粘包测试
//        startHalfPack();
//        startWithLineBasedFrameDecoder();
        startWithDelimiterBasedFrameDecoder();
    }

    //半包测试
    private static void startHalfPack() throws Exception {
        Map<ChannelOption, Object> options = new HashMap<>();
//        options.put(ChannelOption.SO_RCVBUF, 10);//直接设置该参数无用，不会拆包
        options.put(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 16));
        startServer(options, pipeline -> pipeline.addLast(new ServerHandler()));
    }

    //使用行分割修复问题
    private static void startWithLineBasedFrameDecoder() throws Exception {
        Map<ChannelOption, Object> options = new HashMap<>();
//        options.put(ChannelOption.SO_RCVBUF, 10);//直接设置该参数无用，不会拆包
        options.put(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 64));
        startServer(options, pipeline -> pipeline.addLast(
                new LineBasedFrameDecoder(65536),
                new ServerHandler()));
    }

    private static void startWithDelimiterBasedFrameDecoder() throws Exception {
        Map<ChannelOption, Object> options = new HashMap<>();
        options.put(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16, 16, 64));
        startServer(options, pipeline -> {
            ByteBuf buf = pipeline.channel().alloc().buffer();
            buf.writeCharSequence("-_-\n", StandardCharsets.UTF_8);
            ByteBuf buf1 = pipeline.channel().alloc().buffer();
            buf1.writeCharSequence("-_-\r\n", StandardCharsets.UTF_8);
            pipeline.addLast(
                    new DelimiterBasedFrameDecoder(65536, buf, buf1),
                    new ServerHandler()
            );
        });
    }

    private static void startServer(Consumer<ChannelPipeline> pipelineConsumer) throws Exception{
        startServer(new HashMap<>(), pipelineConsumer);
    }

    private static void startServer(Map<ChannelOption, Object> options, Consumer<ChannelPipeline> pipelineConsumer) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(9999))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            pipelineConsumer.accept(ch.pipeline());
                        }
                    });
            for (Map.Entry<ChannelOption, Object> entry: options.entrySet()) {
                bootstrap.childOption(entry.getKey(), entry.getValue());
            }
            ChannelFuture future = bootstrap.bind().addListener(future1 -> {
                if (future1.isSuccess()) {
                    System.out.println(Thread.currentThread().getName() + " 服务器启动成功!");
                } else {
                    future1.cause().printStackTrace();
                }
            });
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}