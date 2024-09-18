package cn.lnd.ibatis.reflection.property;

import java.util.Iterator;

/**
 * @Author lnd
 * @Description 工具类；负责解析由“.”和“[]”构成的表达式。PropertyTokenizer 继承了 Iterator 接口，可以迭代处理嵌套多层表达式。
 *      举个例子，在访问 "order[0].item[0].name" 时，我们希望拆分成 "order[0]"、"item[0]"、"name" 三段，那么就可以通过 PropertyTokenizer 来实现。
 * @Date 2024/9/9 22:43
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {

    /**
     * 当前字符串
     * */
    private String name;

    /**
     * 带索引的 {@link #name} ，因为 {@link #name} 如果存在 {@link #index} 会被更改
     */
    private String indexedName;

    /**
     * 编号。
     *  对于数组 name[0] ，则 index = 0
     *  对于 Map map[key] ，则 index = key
     */
    private String index;

    /**
     * 剩余字符串
     */
    private String children;

    public static void main(String[] args) {
        String fullname = "order[0].item[0].name";
        PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);
        System.out.println(tokenizer.getName());    // order
        System.out.println(tokenizer.getIndexedName()); // order[0]
        System.out.println(tokenizer.getIndex()); // 0
        System.out.println(tokenizer.getChildren()); // item[0].name
    }

    public PropertyTokenizer(String fullname) {
        // <1> 初始化 name、children 字符串，使用 . 作为分隔
        int delim = fullname.indexOf('.'); //字符 . 第一次出现的位置
        if (delim > -1) {
            name = fullname.substring(0, delim);
            children = fullname.substring(delim + 1);
        } else {
            name = fullname;
            children = null;
        }
        // <2> 记录当前 name
        indexedName = name;
        // 若存在 [ ，则获得 index ，并修改 name 。
        delim = name.indexOf('[');
        if (delim > -1) {
            index = name.substring(delim + 1, name.length() - 1);
            name = name.substring(0, delim);
        }
    }

    public String getName() {
        return name;
    }

    public String getIndex() {
        return index;
    }

    public String getIndexedName() {
        return indexedName;
    }

    public String getChildren() {
        return children;
    }

    /**
     * 判断是否有下一个元素
     * @return
     */
    @Override
    public boolean hasNext() {
        return children != null;
    }

    /**
     * 迭代获得下一个 PropertyTokenizer 对象
     * @return
     */
    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }
}
