package cn.lnd.ibatis.builder.xml;

import org.apache.ibatis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * @Author lnd
 * @Description
 *      实现 org.xml.sax.EntityResolver 接口，MyBatis 自定义 EntityResolver 实现类，用于加载本地的 mybatis-3-config.dtd 和 mybatis-3-mapper.dtd 这两个 DTD 文件。
 * @Date 2024/9/18 23:54
 */
public class XMLMapperEntityResolver implements EntityResolver {

    private static final String IBATIS_CONFIG_SYSTEM = "ibatis-3-config.dtd";
    private static final String IBATIS_MAPPER_SYSTEM = "ibatis-3-mapper.dtd";
    private static final String MYBATIS_CONFIG_SYSTEM = "mybatis-3-config.dtd";
    private static final String MYBATIS_MAPPER_SYSTEM = "mybatis-3-mapper.dtd";

    // 本地 mybatis-config.dtd 文件
    private static final String MYBATIS_CONFIG_DTD = "cn/lnd/ibatis/builder/xml/mybatis-3-config.dtd";
    // 本地 mybatis-mapper.dtd 文件
    private static final String MYBATIS_MAPPER_DTD = "cn/lnd/ibatis/builder/xml/mybatis-3-mapper.dtd";

    /*
     * Converts a public DTD into a local one
     *
     * @param publicId The public id that is what comes after "PUBLIC"
     * @param systemId The system id that is what comes after the public id.
     * @return The InputSource for the DTD
     *
     * @throws org.xml.sax.SAXException If anything goes wrong
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        try {
            if (systemId != null) {
                String lowerCaseSystemId = systemId.toLowerCase(Locale.ENGLISH);
                // 本地 mybatis-config.dtd 文件
                if (lowerCaseSystemId.contains(MYBATIS_CONFIG_SYSTEM) || lowerCaseSystemId.contains(IBATIS_CONFIG_SYSTEM)) {
                    return getInputSource(MYBATIS_CONFIG_DTD, publicId, systemId);
                }
                // 本地 mybatis-mapper.dtd 文件
                else if (lowerCaseSystemId.contains(MYBATIS_MAPPER_SYSTEM) || lowerCaseSystemId.contains(IBATIS_MAPPER_SYSTEM)) {
                    return getInputSource(MYBATIS_MAPPER_DTD, publicId, systemId);
                }
            }
            return null;
        } catch (Exception e) {
            throw new SAXException(e.toString());
        }
    }

    /**
     *
     * @param path
     * @param publicId
     * @param systemId
     * @return
     */
    private InputSource getInputSource(String path, String publicId, String systemId) {
        InputSource source = null;
        if (path != null) {
            try {
                // 创建 InputSource 对象
                InputStream in = Resources.getResourceAsStream(path);
                source = new InputSource(in);
                // 设置  publicId、systemId 属性
                source.setPublicId(publicId);
                source.setSystemId(systemId);
            } catch (IOException e) {
                // ignore, null is ok
            }
        }
        return source;
    }

}
