package cn.lnd.ibatis.executor.result;

import cn.lnd.ibatis.reflection.factory.ObjectFactory;
import cn.lnd.ibatis.session.ResultContext;
import cn.lnd.ibatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 20:49
 */
public class DefaultResultHandler implements ResultHandler<Object> {

    private final List<Object> list;

    public DefaultResultHandler() {
        list = new ArrayList<Object>();
    }

    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory) {
        list = objectFactory.create(List.class);
    }

    @Override
    public void handleResult(ResultContext<? extends Object> context) {
        list.add(context.getResultObject());
    }

    public List<Object> getResultList() {
        return list;
    }

}
