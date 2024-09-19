package cn.lnd.ibatis.session;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 14:38
 */
public interface ResultHandler<T> {

    void handleResult(ResultContext<? extends T> resultContext);

}
