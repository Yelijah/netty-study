package cn.ymr.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;
import junit.framework.TestCase;
import org.junit.Assert;

import java.nio.charset.StandardCharsets;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-04
 */
public class ByteBufTest extends TestCase {
    public void test() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(12);
        byteBuf.writeBytes(new byte[] {12, 13, 14});
        byteBuf.writeCharSequence("aaaa我", StandardCharsets.UTF_8);
        assertEquals(11, byteBuf.readableBytes());  //a在utf-8编码下占1个byte，而我占3个
        System.out.println(ByteBufUtil.prettyHexDump(byteBuf));

        byte[] bytes = new byte[4];
        byteBuf.readBytes(bytes);
        Assert.assertArrayEquals(new byte[]{12, 12, 13, 14}, bytes);
        assertEquals("aaaa我", byteBuf.readCharSequence(7, StandardCharsets.UTF_8)); //注意，length参数是指的byte长度而非char长度
        assertFalse(byteBuf.isReadable());  //读索引已经到头了，无数据可读了

        //set/get不受索引控制，也不改变索引
        byteBuf.setByte(0, 0);
        assertEquals(0, byteBuf.getByte(0));
    }

    public void testCapacity() {
        ByteBuf byteBuf = Unpooled.buffer(8, 16);   //初始8B，最大16B
        byteBuf.writeBytes(new byte[]{12, 34, 32, 43, 33, 33, 33, 23});
        byteBuf.writeBytes(new byte[]{22, 98, 66, 22, 21, 1, 3, 3});    //自动扩容
        assertFalse(byteBuf.isWritable());  //无容量可写了
        Assert.assertThrows(IndexOutOfBoundsException.class, () -> byteBuf.writeByte(12));
    }

    public void testMarkIndex() {
        ByteBuf byteBuf = Unpooled.buffer(4, 8);   //初始8B，最大16B
        byteBuf.writeBytes(new byte[]{1, 2, 3, 4});
        byteBuf.markWriterIndex();  //设置写标记位
        byteBuf.writeBytes(new byte[]{5, 6, 7, 8});

        byte[] bytes = new byte[4];
        byteBuf.readBytes(bytes);
        byteBuf.markReaderIndex();  //设置读标记位
        byteBuf.readBytes(bytes);
        Assert.assertArrayEquals(new byte[]{5, 6, 7, 8}, bytes);
        byteBuf.resetReaderIndex(); //重置读索引到标记位，进行重复读取
        byteBuf.readBytes(bytes);
        Assert.assertArrayEquals(new byte[]{5, 6, 7, 8}, bytes);

        byteBuf.resetReaderIndex(); //再次重置读索引到标记位，否则无法重置写索引，因为readerIndex(8) > writeIndex(4)
        byteBuf.resetWriterIndex(); //重置写索引至标记位，进行重复写入
        byteBuf.writeBytes(new byte[]{9, 10, 11, 12});
        byteBuf.getBytes(4, bytes);
        Assert.assertArrayEquals(new byte[]{9, 10, 11, 12}, bytes);
    }

    public void testHeapDirect() {
        //直接缓冲区，读写较快，因为少一次复制到heap中，缺点是释放和建立代价昂贵，往往需要配合池化功能
        ByteBuf directByteBuf = Unpooled.directBuffer();
        System.out.println(directByteBuf.getClass());
        directByteBuf.writeBytes(new byte[]{1, 2, 3, 4});
        assertFalse(directByteBuf.hasArray());
        byte[] array = new byte[directByteBuf.readableBytes()];
        directByteBuf.readBytes(array);
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4}, array);


        //堆缓冲区
        ByteBuf heapByteBuf = Unpooled.buffer(8, 16);
        System.out.println(heapByteBuf.getClass());
        heapByteBuf.writeBytes(new byte[]{1, 2, 3, 4});
        assertTrue(heapByteBuf.hasArray());
        assertEquals(8, heapByteBuf.array().length);
        System.arraycopy(heapByteBuf.array(), 0, array, 0, array.length);
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4}, array);

        CompositeByteBuf compositeByteBuff = ByteBufAllocator.DEFAULT.compositeBuffer();    //默认创建堆内存
        assertFalse(compositeByteBuff.isDirect());
        assertEquals(0, compositeByteBuff.numComponents());
        assertEquals(16, compositeByteBuff.maxNumComponents());
        compositeByteBuff.writeByte(12);
        assertEquals(1, compositeByteBuff.numComponents());
        assertEquals(1, compositeByteBuff.readableBytes());
    }

    public void testRelease() throws Exception {
        ByteBuf heapByteBuf = Unpooled.buffer();
        heapByteBuf.writeBytes(new byte[]{1, 2, 3, 4});
        assertEquals(1, heapByteBuf.refCnt());
        heapByteBuf.release();
        assertEquals(0, heapByteBuf.refCnt());
        Assert.assertThrows(IllegalReferenceCountException.class, () -> heapByteBuf.getByte(0));    //涉及读写的操作会报错
        assertEquals(4, heapByteBuf.readableBytes());   //不涉及内容的读写，只是index的计算，所以不会报错
        assertEquals(0 ,heapByteBuf.refCnt());

        ByteBuf finalHeapByteBuf = Unpooled.buffer();
        finalHeapByteBuf.writeByte(1);
        Thread thread = new Thread(() -> {
            try {
                finalHeapByteBuf.retain();
                finalHeapByteBuf.getByte(0);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " be interrupted!");
            } finally {
                finalHeapByteBuf.release();
            }
        });
        thread.start();
        Thread.sleep(1000);
        assertEquals(2, finalHeapByteBuf.refCnt());
        finalHeapByteBuf.release();
        assertEquals(1, finalHeapByteBuf.refCnt());
        thread.interrupt();
        Thread.sleep(1000);
        assertEquals(0, finalHeapByteBuf.refCnt());
    }
}