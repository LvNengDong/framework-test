package cn.lnd.ibatis.annotations;

import java.lang.annotation.*;

/**
 * @Author lnd
 * @Description
 *
 *      当映射器方法需多个参数，这个注解可以被应用于映射器方法参数来给每个参数一个名字。否则，多参数将会以它们的顺序位置来被命名。比如 #{1}，#{2} 等，这是默认的。

        使用 @Param("person") ，SQL 中参数应该被命名为 #{person} 。
 * @Date 2024/9/19 14:37
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER) // 参数
public @interface Param {

    /**
     * @return 参数名
     */
    String value();
}
