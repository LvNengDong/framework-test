package cn.lnd.ibatis.scripting.defaults;

import cn.lnd.ibatis.builder.BuilderException;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.scripting.xmltags.XMLLanguageDriver;
import cn.lnd.ibatis.session.Configuration;

/**
 * @Author lnd
 * @Description
 *
 *      继承 XMLLanguageDriver 类，RawSqlSource 语言驱动器实现类，确保创建的 SqlSource 是 RawSqlSource 类
 *          先基于父方法，创建 SqlSource 对象，然后再调用 #checkIsNotDynamic(SqlSource source) 方法，进行校验是否为 RawSqlSource 对象。
 *
 * @Date 2024/9/20 21:05
 */
public class RawLanguageDriver extends XMLLanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        // 调用父类，创建 SqlSource 对象
        SqlSource source = super.createSqlSource(configuration, script, parameterType);
        // 校验创建的是 RawSqlSource 对象
        checkIsNotDynamic(source);
        return source;
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        // 调用父类，创建 SqlSource 对象
        SqlSource source = super.createSqlSource(configuration, script, parameterType);
        // 校验创建的是 RawSqlSource 对象
        checkIsNotDynamic(source);
        return source;
    }

    /**
     * 校验是 RawSqlSource 对象
     * */
    private void checkIsNotDynamic(SqlSource source) {
        if (!RawSqlSource.class.equals(source.getClass())) {
            throw new BuilderException("Dynamic content is not allowed when using RAW language");
        }
    }

}
