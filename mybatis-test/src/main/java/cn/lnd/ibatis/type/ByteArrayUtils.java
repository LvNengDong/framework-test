package cn.lnd.ibatis.type;

/**
 * @Author lnd
 * @Description Byte 数组的工具类
 * @Date 2024/9/19 16:21
 */
public class ByteArrayUtils {

    private ByteArrayUtils() {
        // Prevent Instantiation
    }

    // Byte[] => byte[]
    static byte[] convertToPrimitiveArray(Byte[] objects) {
        final byte[] bytes = new byte[objects.length];
        for (int i = 0; i < objects.length; i++) {
            bytes[i] = objects[i];
        }
        return bytes;
    }

    // byte[] => Byte[]
    static Byte[] convertToObjectArray(byte[] bytes) {
        final Byte[] objects = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            objects[i] = bytes[i];
        }
        return objects;
    }
}
