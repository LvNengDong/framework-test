package cn.lnd.tmp;

import lombok.Data;

/**
 * @Author lnd
 * @Description Mapper.xml的映射
 * @Date 2023/2/12 23:05
 */
@Data
public class MappedStatement {
    /**id*/
    private String id;
    /**SQL语句*/
    private String sql;

    /**输入参数*/
    private Class<?> parameterType;
    /**输出参数*/
    private Class<?> resultType;
}
