package cn.lnd.tmp;

import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * @Author lnd
 * @Description 解析Mapper配置文件，并保存到一个内存对象中
 * @Date 2023/2/12 23:56
 */
@Data
public class XMLMapperBuilder {

    private Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 解析Mapper.xml配置文件，并保存到configuration对象中
     *
     * <mapper namespace="User">
     *      <select id="selectOne" paramterType="com.lagou.pojo.User" resultType="com.lagou.pojo.User">
     *          select * from user where id = #{id} and username =#{username}
     *      </select>
     *      <select id="selectList" resultType="com.lagou.pojo.User">
     *          select * from user
     *      </select>
     * </mapper>
     */
    public void parse(InputStream inputStream) throws Exception {
        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();
        String namespace = rootElement.attributeValue("namespace");
        List<Element> select = rootElement.selectNodes("select");
        for (Element element : select) {
            String id = element.attributeValue("id");
            String parameterType = element.attributeValue("parameterType");
            String resultType = element.attributeValue("resultType");
            Class<?> parameterTypeClass = this.getClassType(parameterType);
            Class<?> resultTypeClass = this.getClassType(resultType);
            // statementId
            String key = namespace + "." + id;
            // sql语句(数据未渲染）
            String textTrim = element.getTextTrim();

            // 封装 mappedStatement
            MappedStatement mappedStatement = new MappedStatement();
            mappedStatement.setId(id);
            mappedStatement.setSql(textTrim);
            mappedStatement.setParameterType(parameterTypeClass);
            mappedStatement.setResultType(resultTypeClass);

            // 填充 configuration
            configuration.getMappedStatementMap().put(key, mappedStatement);
        }
    }

    private Class<?> getClassType(String parameterType) throws ClassNotFoundException {
        Class<?> aClass = Class.forName(parameterType);
        return aClass;
    }
}
