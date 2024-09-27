package cn.lnd.ibatis.builder.xml;

import cn.lnd.ibatis.builder.BuilderException;
import cn.lnd.ibatis.builder.IncompleteElementException;
import cn.lnd.ibatis.builder.MapperBuilderAssistant;
import cn.lnd.ibatis.parsing.PropertyParser;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author lnd
 * @Description
 *      在实际应用中，我们会在 <sql/> 标签中定义一些可重用的 SQL 片段，以便在其它语句中使用。
 *      所以在真正解析某个SQL标签（<select/>、<insert/>等）之前，MyBatis 会先将 SQL 标签中的 <include> 标签转换成对应的 SQL 片段（即定义在 <sql> 标签内的文本）
 *      这个转换过程是在 XMLIncludeTransformer.applyIncludes() 方法中实现的（其中不仅包含了 <include> 标签的处理，还包含了“${}”占位符的处理）
 * @Date 2024/9/19 16:52
 */
public class XMLIncludeTransformer {

    private final Configuration configuration;
    private final MapperBuilderAssistant builderAssistant;

    public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    /**
     * 将引用的 <include/> 标签转换成 sql 代码段
     *
     * @param source 某个SQL标签（<select/>、<insert/>等）
     */
    public void applyIncludes(Node source) {
        Properties variablesContext = new Properties();
        Properties configurationVariables = configuration.getVariables();
        if (configurationVariables != null) {
            variablesContext.putAll(configurationVariables);
        }
        applyIncludes(source, variablesContext, false);
    }

    /**
     *
     * @param source 某个SQL标签（<select/>、<insert/>等）
     * @param variablesContext 配置属性上下文
     * @param included
     */
    private void applyIncludes(Node source, final Properties variablesContext, boolean included) {
        if (source.getNodeName().equals("include")) { // SQL 语句中含有 include 标签
            //查找 refid 属性指向的 <sql> 标签，得到其对应的 Node 对象
            Node toInclude = findSqlFragment(getStringAttribute(source, "refid"), variablesContext);
            Properties toIncludeContext = getVariablesContext(source, variablesContext);
            applyIncludes(toInclude, toIncludeContext, true);
            if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
                toInclude = source.getOwnerDocument().importNode(toInclude, true);
            }
            source.getParentNode().replaceChild(toInclude, source);
            while (toInclude.hasChildNodes()) {
                toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
            }
            toInclude.getParentNode().removeChild(toInclude);
        } else if (source.getNodeType() == Node.ELEMENT_NODE) {
            NodeList children = source.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                applyIncludes(children.item(i), variablesContext, included);
            }
        } else if (included && source.getNodeType() == Node.TEXT_NODE
                && !variablesContext.isEmpty()) {
            // replace variables ins all text nodes
            source.setNodeValue(PropertyParser.parse(source.getNodeValue(), variablesContext));
        }
    }

    private Node findSqlFragment(String refid, Properties variables) {
        refid = PropertyParser.parse(refid, variables);
        refid = builderAssistant.applyCurrentNamespace(refid, true);
        try {
            XNode nodeToInclude = configuration.getSqlFragments().get(refid);
            return nodeToInclude.getNode().cloneNode(true);
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("Could not find SQL statement to include with refid '" + refid + "'", e);
        }
    }

    private String getStringAttribute(Node node, String name) {
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    /**
     * Read placholders and their values from include node definition. 
     * @param node Include node instance
     * @param inheritedVariablesContext Current context used for replace variables in new variables values
     * @return variables context from include instance (no inherited values)
     */
    private Properties getVariablesContext(Node node, Properties inheritedVariablesContext) {
        Map<String, String> declaredProperties = null;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = getStringAttribute(n, "name");
                // Replace variables inside
                String value = PropertyParser.parse(getStringAttribute(n, "value"), inheritedVariablesContext);
                if (declaredProperties == null) {
                    declaredProperties = new HashMap<String, String>();
                }
                if (declaredProperties.put(name, value) != null) {
                    throw new BuilderException("Variable " + name + " defined twice in the same include definition");
                }
            }
        }
        if (declaredProperties == null) {
            return inheritedVariablesContext;
        } else {
            Properties newProperties = new Properties();
            newProperties.putAll(inheritedVariablesContext);
            newProperties.putAll(declaredProperties);
            return newProperties;
        }
    }
}
