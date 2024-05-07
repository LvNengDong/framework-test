package cn.lnd.refelct;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/7 15:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CompareDesc {
    /**
     * diff时描述
     *
     * @return
     */
    String desc();
}
