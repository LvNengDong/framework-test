package cn.lnd.ibatis.reflection.invoker;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/4 18:10
 */
public class MethodInvoker implements Invoker {

    // 类型（参数类型或返回值类型）
    private Class<?> type;
    // 指定方法
    private Method method;

    public MethodInvoker(Method method) {
        this.method = method;
        // 参数大小为1时，一般是setting方法，设置type类型为方法参数[0]
        if (method.getParameterTypes().length == 1) {
            type = method.getParameterTypes()[0];
        } else { // 否则，一般是getting方法，设置type位返回值类型
            type = method.getReturnType();
        }
    }

    @Override
    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(target, args);
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
