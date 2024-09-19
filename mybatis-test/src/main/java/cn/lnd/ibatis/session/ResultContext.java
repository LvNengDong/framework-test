package cn.lnd.ibatis.session;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 21:12
 */
public interface ResultContext<T> {

    T getResultObject();

    int getResultCount();

    boolean isStopped();

    void stop();

}
