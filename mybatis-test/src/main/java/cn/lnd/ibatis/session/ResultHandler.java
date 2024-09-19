package cn.lnd.ibatis.session;

import org.apache.ibatis.session.ResultContext;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 14:38
 */
public interface ResultHandler<T> {

    void handleResult(ResultContext<? extends T> resultContext);

}
