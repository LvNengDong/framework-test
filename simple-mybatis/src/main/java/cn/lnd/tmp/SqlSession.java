package cn.lnd.tmp;

import java.util.List;

/**
 * @Author lnd
 * @Description 主要封装CRUD方法
 * @Date 2023/2/12 22:47
 */
public interface SqlSession {

    /**
     * 查询所有
     *  List 前面的 <E> 就相当于一个声明，一般我们把表示泛型的字母放在类上（User<T>），同样，我们可以把这个字母
     *  放在某个具体的方法上。
     */
    <E> List<E> selectList(String statementId, Object... param);

    /**
     * 查询单个
     *   具体调用哪个方法是有 statementId 决定的。
     *   并且在map结构的value中保存着SQL的未渲染版本，我们可以将param参数渲染进去，组成最终SQL
     */
    <T> T selectOne(String statementId, Object... param);

    void close();
}
