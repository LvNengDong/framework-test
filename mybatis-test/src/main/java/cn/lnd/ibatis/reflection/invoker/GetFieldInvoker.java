package cn.lnd.ibatis.reflection.invoker;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/4 18:23
 */
public class GetFieldInvoker implements Invoker {
    private Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return this.field.get(target);
    }

    public Class<?> getType() {
        return this.field.getType();
    }
}
