package cn.lnd.query;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ServiceLoader;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/29 21:22
 */
@Slf4j
public class ReadTest {
    public static void main(String[] args) {
        try {
            // 建立连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mybatis_example", "root", "123456789");
            query(conn);
            queryByParam(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void query(Connection conn) throws Exception {
        Statement statement = conn.createStatement();
        // 执行 SQL 查询，获取返回结果
        ResultSet resultSet = statement.executeQuery("select * from t_user");
        statement
        while (resultSet.next()) {
            int id = resultSet.getInt(1);
            String username = resultSet.getString(2);
            Date createTime = resultSet.getDate(3);
            int age = resultSet.getInt(4);
            log.info(id + username + createTime + age);
        }
    }

    public static void queryByParam(Connection conn) throws Exception {
        // 执行 SQL 查询，获取返回结果
        PreparedStatement preparedStatement = conn.prepareStatement("select * from t_user where id = ? and age = ?");
        preparedStatement.setInt(1, 1);
        preparedStatement.setInt(2, 23);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            int id = resultSet.getInt(1);
            String username = resultSet.getString(2);
            Date createTime = resultSet.getDate(3);
            int age = resultSet.getInt(4);
            log.info(id + username + createTime + age);
        }
    }


}
