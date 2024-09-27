package cn.lnd.ibatis.mapping;

import cn.lnd.ibatis.session.Configuration;

import java.util.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/19 15:15
 */
public class ResultMap {
    /* 当前 <resultMap> 标签的 id 属性值 */
    private String id;
    /* 当前 <resultMap> 的 type 属性值 */
    private Class<?> type;
    /* 维护了整个<resultMap> 标签解析之后得到的全部映射关系，也就是全部 ResultMapping 对象。 */
    private List<ResultMapping> resultMappings;
    /* 维护了与唯一标识相关的映射，例如，<id> 标签、<constructor> 标签下的 <idArg> 子标签解析得到的 ResultMapping 对象。如果没有定义 <id> 等唯一性标签，则由 resultMappings 集合中全部映射关系来确定一条记录的唯一性，即 idResultMappings 集合与 resulMappings 集合相同。 */
    private List<ResultMapping> idResultMappings;
    /* 维护了 <constructor> 标签下全部子标签定义的映射关系。 */
    private List<ResultMapping> constructorResultMappings;
    /* 维护了不带 Constructor 标志的映射关系。 */
    private List<ResultMapping> propertyResultMappings;
    /* 维护了所有映射关系中涉及的 column 属性值，也就是所有的列名（或别名） */
    private Set<String> mappedColumns;
    /* 对应 <discriminator> 标签 */
    private Discriminator discriminator;
    /* 当前 <resultMap> 标签是否嵌套了其他 <resultMap> 标签 */
    private boolean hasNestedResultMaps;
    /* 当前 <resultMap> 标签是否含有嵌套查询 */
    private boolean hasNestedQueries;
    /* 当前 ResultMap 是否开启自动映射的功能 */
    private Boolean autoMapping;

    private ResultMap() {
    }

    public static class Builder {
        private ResultMap resultMap = new ResultMap();

        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
            this(configuration, id, type, resultMappings, null);
        }

        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings, Boolean autoMapping) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
            resultMap.autoMapping = autoMapping;
        }

        public ResultMap.Builder discriminator(Discriminator discriminator) {
            resultMap.discriminator = discriminator;
            return this;
        }

        public Class<?> type() {
            return resultMap.type;
        }

        public ResultMap build() {
            if (resultMap.id == null) {
                throw new IllegalArgumentException("ResultMaps must have an id");
            }
            resultMap.mappedColumns = new HashSet<String>();
            resultMap.idResultMappings = new ArrayList<ResultMapping>();
            resultMap.constructorResultMappings = new ArrayList<ResultMapping>();
            resultMap.propertyResultMappings = new ArrayList<ResultMapping>();
            for (ResultMapping resultMapping : resultMap.resultMappings) {
                resultMap.hasNestedQueries = resultMap.hasNestedQueries || resultMapping.getNestedQueryId() != null;
                resultMap.hasNestedResultMaps = resultMap.hasNestedResultMaps || (resultMapping.getNestedResultMapId() != null && resultMapping.getResultSet() == null);
                final String column = resultMapping.getColumn();
                if (column != null) {
                    resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                } else if (resultMapping.isCompositeResult()) {
                    for (ResultMapping compositeResultMapping : resultMapping.getComposites()) {
                        final String compositeColumn = compositeResultMapping.getColumn();
                        if (compositeColumn != null) {
                            resultMap.mappedColumns.add(compositeColumn.toUpperCase(Locale.ENGLISH));
                        }
                    }
                }
                if (resultMapping.getFlags().contains(ResultFlag.CONSTRUCTOR)) {
                    resultMap.constructorResultMappings.add(resultMapping);
                } else {
                    resultMap.propertyResultMappings.add(resultMapping);
                }
                if (resultMapping.getFlags().contains(ResultFlag.ID)) {
                    resultMap.idResultMappings.add(resultMapping);
                }
            }
            if (resultMap.idResultMappings.isEmpty()) {
                resultMap.idResultMappings.addAll(resultMap.resultMappings);
            }
            // lock down collections
            resultMap.resultMappings = Collections.unmodifiableList(resultMap.resultMappings);
            resultMap.idResultMappings = Collections.unmodifiableList(resultMap.idResultMappings);
            resultMap.constructorResultMappings = Collections.unmodifiableList(resultMap.constructorResultMappings);
            resultMap.propertyResultMappings = Collections.unmodifiableList(resultMap.propertyResultMappings);
            resultMap.mappedColumns = Collections.unmodifiableSet(resultMap.mappedColumns);
            return resultMap;
        }
    }

    public String getId() {
        return id;
    }

    public boolean hasNestedResultMaps() {
        return hasNestedResultMaps;
    }

    public boolean hasNestedQueries() {
        return hasNestedQueries;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    public List<ResultMapping> getConstructorResultMappings() {
        return constructorResultMappings;
    }

    public List<ResultMapping> getPropertyResultMappings() {
        return propertyResultMappings;
    }

    public List<ResultMapping> getIdResultMappings() {
        return idResultMappings;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }

    public Discriminator getDiscriminator() {
        return discriminator;
    }

    public void forceNestedResultMaps() {
        hasNestedResultMaps = true;
    }

    public Boolean getAutoMapping() {
        return autoMapping;
    }

}
