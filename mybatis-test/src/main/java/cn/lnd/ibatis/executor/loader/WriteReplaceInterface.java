package cn.lnd.ibatis.executor.loader;

import java.io.ObjectStreamException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 20:47
 */
public interface WriteReplaceInterface {

    Object writeReplace() throws ObjectStreamException;

}
