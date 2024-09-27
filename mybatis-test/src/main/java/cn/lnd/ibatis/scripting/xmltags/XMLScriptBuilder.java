package cn.lnd.ibatis.scripting.xmltags;

import cn.lnd.ibatis.builder.BaseBuilder;
import cn.lnd.ibatis.builder.BuilderException;
import cn.lnd.ibatis.mapping.SqlSource;
import cn.lnd.ibatis.parsing.XNode;
import cn.lnd.ibatis.scripting.defaults.RawSqlSource;
import cn.lnd.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lnd
 * @Description
 *
 *      继承 BaseBuilder 抽象类，XML 动态语句( SQL )构建器，负责将 SQL 解析成 SqlSource 对象。
 *
 * @Date 2024/9/20 21:04
 */
public class XMLScriptBuilder extends BaseBuilder {

    /* 当前 SQL 的 XNode 对象 */
    private XNode context;
    /* 是否为动态 SQL */
    private boolean isDynamic;
    /* SQL 方法类型 */
    private Class<?> parameterType;
    /* NodeHandler 的映射 */
    private final Map<String, XMLScriptBuilder.NodeHandler> nodeHandlerMap = new HashMap<>();

    public XMLScriptBuilder(Configuration configuration, XNode context) {
        this(configuration, context, null);
    }

    public XMLScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType) {
        super(configuration);
        this.context = context;
        this.parameterType = parameterType;
        // 初始化 nodeHandlerMap 属性
        initNodeHandlerMap();
    }


    /**
     *  我们可以看到，nodeHandlerMap 的 KEY 是熟悉的 MyBatis 的自定义的 XML 标签。
     *  并且，每个标签对应专属的一个 NodeHandler 实现类。
     * */
    private void initNodeHandlerMap() {
        nodeHandlerMap.put("trim", new XMLScriptBuilder.TrimHandler());
        nodeHandlerMap.put("where", new XMLScriptBuilder.WhereHandler());
        nodeHandlerMap.put("set", new XMLScriptBuilder.SetHandler());
        nodeHandlerMap.put("foreach", new XMLScriptBuilder.ForEachHandler());
        nodeHandlerMap.put("if", new XMLScriptBuilder.IfHandler());
        nodeHandlerMap.put("choose", new XMLScriptBuilder.ChooseHandler());
        nodeHandlerMap.put("when", new XMLScriptBuilder.IfHandler());
        nodeHandlerMap.put("otherwise", new XMLScriptBuilder.OtherwiseHandler());
        nodeHandlerMap.put("bind", new XMLScriptBuilder.BindHandler());
    }

    /**
     * 负责将 SQL 解析成 SqlSource 对象
     * */
    public SqlSource parseScriptNode() {
        // <1> 解析 SQL
        MixedSqlNode rootSqlNode = parseDynamicTags(context);
        // <2> 创建 SqlSource 对象 【根据是否是动态 SQL ，创建对应的 DynamicSqlSource 或 RawSqlSource 对象】
        SqlSource sqlSource = null;
        if (isDynamic) {
            sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
        } else {
            sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
        }
        return sqlSource;
    }

    /**
     * 解析 SQL 成 MixedSqlNode 对象
     * */
    protected MixedSqlNode parseDynamicTags(XNode node) {
        // <1> 创建 SqlNode 数组
        List<SqlNode> contents = new ArrayList<>();
        // <2> 遍历 SQL 节点的所有子节点
        NodeList children = node.getNode().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            // 当前子节点
            XNode child = node.newXNode(children.item(i));
            // <2.1> 如果类型是 Node.CDATA_SECTION_NODE 或者 Node.TEXT_NODE 时
            if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
                // <2.1.1> 获得内容
                String data = child.getStringBody("");
                // <2.1.2> 创建 TextSqlNode 对象
                TextSqlNode textSqlNode = new TextSqlNode(data);
                // <2.1.2.1> 如果是动态的 TextSqlNode 对象
                if (textSqlNode.isDynamic()) {
                    // 添加到 contents 中
                    contents.add(textSqlNode);
                    // 标记为动态 SQL
                    isDynamic = true;
                    // <2.1.2.2> 如果是非动态的 TextSqlNode 对象
                } else {
                    // <2.1.2> 创建 StaticTextSqlNode 添加到 contents 中
                    contents.add(new StaticTextSqlNode(data));
                }
                // <2.2> 如果类型是 Node.ELEMENT_NODE
            } else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) { // issue #628
                // <2.2.1> 根据子节点的标签，获得对应的 NodeHandler 对象
                String nodeName = child.getNode().getNodeName();
                NodeHandler handler = nodeHandlerMap.get(nodeName);
                if (handler == null) { // 获得不到，说明是未知的标签，抛出 BuilderException 异常
                    throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
                }
                // <2.2.2> 执行 NodeHandler 处理
                handler.handleNode(child, contents);
                // <2.2.3> 标记为动态 SQL
                isDynamic = true;
            }
        }
        // <3> 创建 MixedSqlNode 对象
        return new MixedSqlNode(contents);
    }

    private interface NodeHandler {
        void handleNode(XNode nodeToHandle, List<SqlNode> targetContents);
    }

    private class BindHandler implements XMLScriptBuilder.NodeHandler {
        public BindHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            final String name = nodeToHandle.getStringAttribute("name");
            final String expression = nodeToHandle.getStringAttribute("value");
            final VarDeclSqlNode node = new VarDeclSqlNode(name, expression);
            targetContents.add(node);
        }
    }

    private class TrimHandler implements XMLScriptBuilder.NodeHandler {
        public TrimHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
            String prefix = nodeToHandle.getStringAttribute("prefix");
            String prefixOverrides = nodeToHandle.getStringAttribute("prefixOverrides");
            String suffix = nodeToHandle.getStringAttribute("suffix");
            String suffixOverrides = nodeToHandle.getStringAttribute("suffixOverrides");
            TrimSqlNode trim = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
            targetContents.add(trim);
        }
    }

    private class WhereHandler implements XMLScriptBuilder.NodeHandler {
        public WhereHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
            WhereSqlNode where = new WhereSqlNode(configuration, mixedSqlNode);
            targetContents.add(where);
        }
    }

    private class SetHandler implements XMLScriptBuilder.NodeHandler {
        public SetHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
            SetSqlNode set = new SetSqlNode(configuration, mixedSqlNode);
            targetContents.add(set);
        }
    }

    private class ForEachHandler implements XMLScriptBuilder.NodeHandler {
        public ForEachHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
            String collection = nodeToHandle.getStringAttribute("collection");
            String item = nodeToHandle.getStringAttribute("item");
            String index = nodeToHandle.getStringAttribute("index");
            String open = nodeToHandle.getStringAttribute("open");
            String close = nodeToHandle.getStringAttribute("close");
            String separator = nodeToHandle.getStringAttribute("separator");
            ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration, mixedSqlNode, collection, index, item, open, close, separator);
            targetContents.add(forEachSqlNode);
        }
    }

    private class IfHandler implements XMLScriptBuilder.NodeHandler {
        public IfHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
            String test = nodeToHandle.getStringAttribute("test");
            IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
            targetContents.add(ifSqlNode);
        }
    }

    private class OtherwiseHandler implements XMLScriptBuilder.NodeHandler {
        public OtherwiseHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
            targetContents.add(mixedSqlNode);
        }
    }

    private class ChooseHandler implements XMLScriptBuilder.NodeHandler {
        public ChooseHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
            List<SqlNode> whenSqlNodes = new ArrayList<>();
            List<SqlNode> otherwiseSqlNodes = new ArrayList<>();
            handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
            SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
            ChooseSqlNode chooseSqlNode = new ChooseSqlNode(whenSqlNodes, defaultSqlNode);
            targetContents.add(chooseSqlNode);
        }

        private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<SqlNode> ifSqlNodes, List<SqlNode> defaultSqlNodes) {
            List<XNode> children = chooseSqlNode.getChildren();
            for (XNode child : children) {
                String nodeName = child.getNode().getNodeName();
                XMLScriptBuilder.NodeHandler handler = nodeHandlerMap.get(nodeName);
                if (handler instanceof XMLScriptBuilder.IfHandler) {
                    handler.handleNode(child, ifSqlNodes);
                } else if (handler instanceof XMLScriptBuilder.OtherwiseHandler) {
                    handler.handleNode(child, defaultSqlNodes);
                }
            }
        }

        private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
            SqlNode defaultSqlNode = null;
            if (defaultSqlNodes.size() == 1) {
                defaultSqlNode = defaultSqlNodes.get(0);
            } else if (defaultSqlNodes.size() > 1) {
                throw new BuilderException("Too many default (otherwise) elements in choose statement.");
            }
            return defaultSqlNode;
        }
    }

}
