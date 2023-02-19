package cn.lnd.tmp;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/12 22:46
 */
public interface SqlSessionFactory {
    /**获取SqlSession接口的实现类实例对象*/
    SqlSession openSession();
}
