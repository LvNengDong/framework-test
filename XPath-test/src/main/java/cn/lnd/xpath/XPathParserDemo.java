package cn.lnd.xpath;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
//更多请阅读：https://www.yiibai.com/java_xml/java_xpath_parse_document.html


/**
 * @Author lnd
 * @Description
 * @Date 2024/9/20 15:49
 */
public class XPathParserDemo {
    public static void main(String[] args) throws Exception {



        //创建DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        //从文件或数据流创建一个文档
        //StringBuilder xmlStringBuilder = new StringBuilder();
        //xmlStringBuilder.append("\"<?xml version=\"1.0\"?> <class> </class>\"\n");
        //ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        //Document doc = builder.parse(input);

        File inputFile = new File("/Volumes/DEV/workspace/framework-test/XPath-test/src/main/resources/input.txt");
        Document doc = builder.parse(inputFile);
        doc.getDocumentElement().normalize();

        //构建XPath
        XPath xPath =  XPathFactory.newInstance().newXPath();

        //准备路径表达式，并计算它
        String expression = "/class/student";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            System.out.println("\nCurrent Element :"
                    + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                System.out.println("Student roll no : "
                        + eElement.getAttribute("rollno"));
                System.out.println("First Name : "
                        + eElement
                        .getElementsByTagName("firstname")
                        .item(0)
                        .getTextContent());
                System.out.println("Last Name : "
                        + eElement
                        .getElementsByTagName("lastname")
                        .item(0)
                        .getTextContent());
                System.out.println("Nick Name : "
                        + eElement
                        .getElementsByTagName("nickname")
                        .item(0)
                        .getTextContent());
                System.out.println("Marks : "
                        + eElement
                        .getElementsByTagName("marks")
                        .item(0)
                        .getTextContent());
            }
        }
    }
}
