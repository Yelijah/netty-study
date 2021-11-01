package cn.ymr.nio;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Desc
 *
 * @author Elijah
 * created on 2021-11-01
 */
public class ByteBufferUtils {
    public static String toString(ByteBuffer buffer) {
        buffer.flip();
        String s = StandardCharsets.UTF_8.decode(buffer).toString();
        buffer.clear();
        return s;
    }
}