package cn.lnd;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.Reader;

/**
 * @Author lnd
 * @Description
 * @Date 2023/2/10 00:39
 */
public class App {
    public static void main(String[] args) {
        String resource = "mybatis-config.xml";
        Reader reader;
        SqlSession session = null;
        try {
            //将XML配置文件构建为Configuration配置类
            reader = Resources.getResourceAsReader(resource);
            // 通过加载配置文件构建一个SQLSessionFactory DefaultSqlSessionFactory
            SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
            //数据源 执行器 DefaultSqlSession
            session = sqlMapper.openSession();
            //执行查询（底层执行JDBC）
            UserMapper mapper = session.getMapper(UserMapper.class);
            System.out.println(mapper.getClass());
            //User user = mapper.selectById(1L);
            //System.out.println(user.getUsername());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            session.close();
        }
    }
}
