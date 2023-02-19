package cn.lnd.tmp;

import lombok.Data;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/13 00:29
 */
@Data
public class SimpleExecutor implements Executor{

    private Connection connection = null;
    @Override
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object[] param) throws Exception {
        //获取连接
        connection = configuration.getDataSource().getConnection();
        //对SQL进行处理
        String sql = "";
        BoundSql boundSql = getBoundSql(sql);
        String finalSql = boundSql.getSqlText(sql);
        //获取传入参数类型
        Class<?> parameterType = mappedStatement.getParameterType();
        //获取预编译preparedStatement对象
        PreparedStatement preparedStatement = connection.prepareStatement(finalSql);
        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappingList();
        for (int i = 0; i < parameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = parameterMappingList.get(i);
            String name = parameterMapping.getName();
            //反射
            Field declaredField = parameterType.getDeclaredField(name);
            declaredField.setAccessible(true);
            //参数的值

        }
        //Class<?> parameterType = mappedStatement.getParameterType();
        return null;
    }

    private BoundSql getBoundSql(String sql) {
        return null;
    }

    @Override
    public void close() {

    }
}
