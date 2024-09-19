package cn.lnd.ibatis.session;

import cn.lnd.ibatis.logging.Log;
import cn.lnd.ibatis.logging.LogFactory;
import cn.lnd.ibatis.mapping.MappedStatement;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 21:10
 */
public enum AutoMappingUnknownColumnBehavior {

    /**
     * Do nothing (Default).
     */
    NONE {
        @Override
        public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            // do nothing
        }
    },

    /**
     * Output warning log.
     * Note: The log level of {@code 'cn.lnd.ibatis.session.AutoMappingUnknownColumnBehavior'} must be set to {@code WARN}.
     */
    WARNING {
        @Override
        public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            log.warn(buildMessage(mappedStatement, columnName, property, propertyType));
        }
    },

    /**
     * Fail mapping.
     * Note: throw {@link SqlSessionException}.
     */
    FAILING {
        @Override
        public void doAction(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
            throw new SqlSessionException(buildMessage(mappedStatement, columnName, property, propertyType));
        }
    };

    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(cn.lnd.ibatis.session.AutoMappingUnknownColumnBehavior.class);

    /**
     * Perform the action when detects an unknown column (or unknown property type) of automatic mapping target.
     * @param mappedStatement current mapped statement
     * @param columnName column name for mapping target
     * @param propertyName property name for mapping target
     * @param propertyType property type for mapping target (If this argument is not null, {@link cn.lnd.ibatis.type.TypeHandler} for property type is not registered)
     */
    public abstract void doAction(MappedStatement mappedStatement, String columnName, String propertyName, Class<?> propertyType);

    /**
     * build error message.
     */
    private static String buildMessage(MappedStatement mappedStatement, String columnName, String property, Class<?> propertyType) {
        return new StringBuilder("Unknown column is detected on '")
                .append(mappedStatement.getId())
                .append("' auto-mapping. Mapping parameters are ")
                .append("[")
                .append("columnName=").append(columnName)
                .append(",").append("propertyName=").append(property)
                .append(",").append("propertyType=").append(propertyType != null ? propertyType.getName() : null)
                .append("]")
                .toString();
    }

}
