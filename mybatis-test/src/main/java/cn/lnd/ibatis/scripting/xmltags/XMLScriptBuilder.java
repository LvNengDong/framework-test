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
 * @Date 2024/9/20 21:04
 */
public class XMLScriptBuilder extends BaseBuilder {

    private XNode context;
    private boolean isDynamic;
    private Class<?> parameterType;

    public XMLScriptBuilder(Configuration configuration, XNode context) {
        this(configuration, context, null);
    }

    public XMLScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType) {
        super(configuration);
        this.context = context;
        this.parameterType = parameterType;
    }

    public SqlSource parseScriptNode() {
        List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = parseDynamicTags(context);
        cn.lnd.ibatis.scripting.xmltags.MixedSqlNode rootSqlNode = new cn.lnd.ibatis.scripting.xmltags.MixedSqlNode(contents);
        SqlSource sqlSource = null;
        if (isDynamic) {
            sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
        } else {
            sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
        }
        return sqlSource;
    }

    List<cn.lnd.ibatis.scripting.xmltags.SqlNode> parseDynamicTags(XNode node) {
        List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = new ArrayList<cn.lnd.ibatis.scripting.xmltags.SqlNode>();
        NodeList children = node.getNode().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            XNode child = node.newXNode(children.item(i));
            if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
                String data = child.getStringBody("");
                cn.lnd.ibatis.scripting.xmltags.TextSqlNode textSqlNode = new TextSqlNode(data);
                if (textSqlNode.isDynamic()) {
                    contents.add(textSqlNode);
                    isDynamic = true;
                } else {
                    contents.add(new StaticTextSqlNode(data));
                }
            } else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) { // issue #628
                String nodeName = child.getNode().getNodeName();
                cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler handler = nodeHandlers(nodeName);
                if (handler == null) {
                    throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
                }
                handler.handleNode(child, contents);
                isDynamic = true;
            }
        }
        return contents;
    }

    cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler nodeHandlers(String nodeName) {
        Map<String, cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler> map = new HashMap<String, cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler>();
        map.put("trim", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.TrimHandler());
        map.put("where", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.WhereHandler());
        map.put("set", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.SetHandler());
        map.put("foreach", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.ForEachHandler());
        map.put("if", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.IfHandler());
        map.put("choose", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.ChooseHandler());
        map.put("when", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.IfHandler());
        map.put("otherwise", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.OtherwiseHandler());
        map.put("bind", new cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.BindHandler());
        return map.get(nodeName);
    }

    private interface NodeHandler {
        void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents);
    }

    private class BindHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public BindHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            final String name = nodeToHandle.getStringAttribute("name");
            final String expression = nodeToHandle.getStringAttribute("value");
            final cn.lnd.ibatis.scripting.xmltags.VarDeclSqlNode node = new VarDeclSqlNode(name, expression);
            targetContents.add(node);
        }
    }

    private class TrimHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public TrimHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = parseDynamicTags(nodeToHandle);
            cn.lnd.ibatis.scripting.xmltags.MixedSqlNode mixedSqlNode = new cn.lnd.ibatis.scripting.xmltags.MixedSqlNode(contents);
            String prefix = nodeToHandle.getStringAttribute("prefix");
            String prefixOverrides = nodeToHandle.getStringAttribute("prefixOverrides");
            String suffix = nodeToHandle.getStringAttribute("suffix");
            String suffixOverrides = nodeToHandle.getStringAttribute("suffixOverrides");
            cn.lnd.ibatis.scripting.xmltags.TrimSqlNode trim = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
            targetContents.add(trim);
        }
    }

    private class WhereHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public WhereHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = parseDynamicTags(nodeToHandle);
            cn.lnd.ibatis.scripting.xmltags.MixedSqlNode mixedSqlNode = new cn.lnd.ibatis.scripting.xmltags.MixedSqlNode(contents);
            cn.lnd.ibatis.scripting.xmltags.WhereSqlNode where = new WhereSqlNode(configuration, mixedSqlNode);
            targetContents.add(where);
        }
    }

    private class SetHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public SetHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = parseDynamicTags(nodeToHandle);
            cn.lnd.ibatis.scripting.xmltags.MixedSqlNode mixedSqlNode = new cn.lnd.ibatis.scripting.xmltags.MixedSqlNode(contents);
            cn.lnd.ibatis.scripting.xmltags.SetSqlNode set = new SetSqlNode(configuration, mixedSqlNode);
            targetContents.add(set);
        }
    }

    private class ForEachHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public ForEachHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = parseDynamicTags(nodeToHandle);
            cn.lnd.ibatis.scripting.xmltags.MixedSqlNode mixedSqlNode = new cn.lnd.ibatis.scripting.xmltags.MixedSqlNode(contents);
            String collection = nodeToHandle.getStringAttribute("collection");
            String item = nodeToHandle.getStringAttribute("item");
            String index = nodeToHandle.getStringAttribute("index");
            String open = nodeToHandle.getStringAttribute("open");
            String close = nodeToHandle.getStringAttribute("close");
            String separator = nodeToHandle.getStringAttribute("separator");
            cn.lnd.ibatis.scripting.xmltags.ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration, mixedSqlNode, collection, index, item, open, close, separator);
            targetContents.add(forEachSqlNode);
        }
    }

    private class IfHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public IfHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = parseDynamicTags(nodeToHandle);
            cn.lnd.ibatis.scripting.xmltags.MixedSqlNode mixedSqlNode = new cn.lnd.ibatis.scripting.xmltags.MixedSqlNode(contents);
            String test = nodeToHandle.getStringAttribute("test");
            cn.lnd.ibatis.scripting.xmltags.IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
            targetContents.add(ifSqlNode);
        }
    }

    private class OtherwiseHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public OtherwiseHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> contents = parseDynamicTags(nodeToHandle);
            cn.lnd.ibatis.scripting.xmltags.MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
            targetContents.add(mixedSqlNode);
        }
    }

    private class ChooseHandler implements cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler {
        public ChooseHandler() {
            // Prevent Synthetic Access
        }

        @Override
        public void handleNode(XNode nodeToHandle, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> targetContents) {
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> whenSqlNodes = new ArrayList<cn.lnd.ibatis.scripting.xmltags.SqlNode>();
            List<cn.lnd.ibatis.scripting.xmltags.SqlNode> otherwiseSqlNodes = new ArrayList<cn.lnd.ibatis.scripting.xmltags.SqlNode>();
            handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
            cn.lnd.ibatis.scripting.xmltags.SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
            cn.lnd.ibatis.scripting.xmltags.ChooseSqlNode chooseSqlNode = new ChooseSqlNode(whenSqlNodes, defaultSqlNode);
            targetContents.add(chooseSqlNode);
        }

        private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> ifSqlNodes, List<cn.lnd.ibatis.scripting.xmltags.SqlNode> defaultSqlNodes) {
            List<XNode> children = chooseSqlNode.getChildren();
            for (XNode child : children) {
                String nodeName = child.getNode().getNodeName();
                cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.NodeHandler handler = nodeHandlers(nodeName);
                if (handler instanceof cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.IfHandler) {
                    handler.handleNode(child, ifSqlNodes);
                } else if (handler instanceof cn.lnd.ibatis.scripting.xmltags.XMLScriptBuilder.OtherwiseHandler) {
                    handler.handleNode(child, defaultSqlNodes);
                }
            }
        }

        private cn.lnd.ibatis.scripting.xmltags.SqlNode getDefaultSqlNode(List<cn.lnd.ibatis.scripting.xmltags.SqlNode> defaultSqlNodes) {
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
