package cn.lnd.ibatis.mapping;

import cn.lnd.ibatis.mapping.ParameterMapping;
import cn.lnd.ibatis.session.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 15:11
 */
public class ParameterMap {

    private String id;
    private Class<?> type;
    private List<cn.lnd.ibatis.mapping.ParameterMapping> parameterMappings;

    private ParameterMap() {
    }

    public static class Builder {
        private cn.lnd.ibatis.mapping.ParameterMap parameterMap = new cn.lnd.ibatis.mapping.ParameterMap();

        public Builder(Configuration configuration, String id, Class<?> type, List<cn.lnd.ibatis.mapping.ParameterMapping> parameterMappings) {
            parameterMap.id = id;
            parameterMap.type = type;
            parameterMap.parameterMappings = parameterMappings;
        }

        public Class<?> type() {
            return parameterMap.type;
        }

        public cn.lnd.ibatis.mapping.ParameterMap build() {
            //lock down collections
            parameterMap.parameterMappings = Collections.unmodifiableList(parameterMap.parameterMappings);
            return parameterMap;
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

}
