package cn.ymr.netty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-04
 */
public class CodecClient {
    public static void main(String[] args) throws Exception {
//        startHalfPack();
//        startWithLine();
        startWithDelimiter();
    }

    private static void startStickPack() throws Exception{
        startClient(ctx -> {
                for (int i = 0; i < 10; i++) {
                    ByteBuf buffer = ctx.alloc().buffer();
                    buffer.writeCharSequence("aaaabbbccc", StandardCharsets.UTF_8);
                    ctx.writeAndFlush(buffer);
                }
            }
        );
    }

    private static void startHalfPack() throws Exception {
        startClient(ctx -> {
                ByteBuf buffer = ctx.alloc().buffer();
                for (int i = 0; i < 10; i++) {
                    buffer.writeCharSequence("aaaabbbcccddffffffgffffffffffffffffffffffffffffffffffffffffffhhhhhhhh", StandardCharsets.UTF_8);
                }
                ctx.writeAndFlush(buffer);
            }
        );
    }

    private static void startWithLine() throws Exception {
        startClient(ctx -> {
            ByteBuf buf = ctx.alloc().buffer();
            for (int i = 0; i < 10; i++) {
                buf.writeCharSequence("aaaabbbccc", StandardCharsets.UTF_8);
                ByteBuf buffer = ctx.alloc().buffer();
                buffer.writeCharSequence("aaaabbbccc\n", StandardCharsets.UTF_8);
                ctx.writeAndFlush(buffer);
            }
            buf.writeCharSequence("\n", StandardCharsets.UTF_8);
            ctx.writeAndFlush(buf);
        });
    }

    private static void startWithDelimiter() throws Exception {
        startClient(ctx -> {
            ByteBuf buf = ctx.alloc().buffer();
            for (int i = 0; i < 10; i++) {
                buf.writeCharSequence("aaaabbbccc", StandardCharsets.UTF_8);
                ByteBuf buffer = ctx.alloc().buffer();
                buffer.writeCharSequence("aaaabbbccc-_-\n", StandardCharsets.UTF_8);
                ctx.writeAndFlush(buffer);
            }
            buf.writeCharSequence("-_-\n", StandardCharsets.UTF_8);
            ctx.writeAndFlush(buf);
        });
    }

    private static void startClient(Consumer<ChannelHandlerContext> consumer, ChannelHandler... handlers) throws Exception{
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .channel(NioSocketChannel.class)
                    .group(group)
                    .remoteAddress("127.0.0.1", 9999)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("client active!");
                                    System.out.println("client send msg begin!");
                                    consumer.accept(ctx);
                                    System.out.println("client send msg over!");
                                    super.channelActive(ctx);
                                }
                            });
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();
            Thread.sleep(3000);
            future.channel().close().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}