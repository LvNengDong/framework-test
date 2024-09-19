package cn.lnd.ibatis.builder.annotation;

import java.lang.reflect.Method;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 16:50
 */
public class MethodResolver {
    private final MapperAnnotationBuilder annotationBuilder;
    private Method method;

    public MethodResolver(MapperAnnotationBuilder annotationBuilder, Method method) {
        this.annotationBuilder = annotationBuilder;
        this.method = method;
    }

    public void resolve() {
        annotationBuilder.parseStatement(method);
    }

}
