package cn.ymr.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-01
 */
public class NioServer {
    public static void main(String[] args) {
//        startNoBlockServer();
        startSelectorNoBlockServer();
    }

    public static void startBlockServer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try (ServerSocketChannel server = ServerSocketChannel.open()){
            server.bind(new InetSocketAddress(9999));
            List<SocketChannel> channels = new ArrayList<>();
            // 循环接收连接
            while (true) {
                System.out.println("before connecting...");
                // 没有连接时，会阻塞线程
                SocketChannel channel = server.accept();
                System.out.println("after connecting...");
                channels.add(channel);
                // 循环遍历集合中的连接, 处理连接中的数据
                for (SocketChannel socketChannel: channels) {
                    System.out.println("before reading..." + socketChannel.getRemoteAddress().toString());
                    // 当通道中没有数据可读时，会阻塞线程
                    socketChannel.read(buffer);
                    System.out.println("read:"+ ByteBufferUtils.toString(buffer));
                    System.out.println("after reading..." + socketChannel.getRemoteAddress().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startNoBlockServer() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(9999));
            server.configureBlocking(false);
            List<SocketChannel> channels = new ArrayList<>();
            //需要一直轮询去处理，且线程不会阻塞，cpu一直在忙碌
            while (true) {
                // 没有连接时，不会阻塞线程
                SocketChannel newChannel = server.accept();
                if (newChannel != null) {
                    System.out.println("after connecting..." + newChannel.getRemoteAddress().toString());
                    newChannel.configureBlocking(false);
                    channels.add(newChannel);
                }
                for (SocketChannel channel: channels) {
                    //没有消息时，不会阻塞线程
                    if (channel.read(buffer) > 0) {
                        System.out.println(channel.getRemoteAddress().toString() + " read:" + ByteBufferUtils.toString(buffer));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startSelectorNoBlockServer() {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(9999));
            server.configureBlocking(false);
            Selector selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                // 若没有事件就绪，线程会被阻塞直到有事件，反之不会被阻塞。从而避免了CPU空转
                int ready = selector.select();
                System.out.println("selector ready count:" + ready);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        // key是绑定的serverChannel
                        assert key.channel() == server;
                        // 获取连接并处理，而且是必须处理或者取消cancel()，否则下次该事件仍会触发
                        SocketChannel channel = server.accept();
                        System.out.println("after connecting..." + channel.getRemoteAddress().toString());
                        //不移除的话。下次select()始终是0, 但是selectedKeys()的结果还是正确的
                        iterator.remove();
                        //给客户端设置非阻塞，且将通道的读事件也注册到selector中
                        channel.configureBlocking(false);
                        //为通道创建单独bytebuffer，作为附件，避免多个channel共用一个bytebuffer
                        channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(16));
                    } else if (key.isReadable()) {
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        //此key绑定的是客户端channel
                        SocketChannel channel = (SocketChannel) key.channel();
                        int c = 0;
                        try {
                            c = channel.read(buffer);
                        } catch (IOException e) {
                            //异常断开连接时，抛出IOException
                            e.printStackTrace();
                        }
                        // 正常断开连接时，客户端会向服务器发送一个写事件，此时read的返回值为-1
                        if (c == -1 || c == 0) {
                            System.out.println(channel.getRemoteAddress().toString() + " close.");
                            key.cancel();
                            channel.close();
                        } else {
                            System.out.println(channel.getRemoteAddress().toString() + " read:" + ByteBufferUtils.toString(buffer));
                        }
                        iterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}