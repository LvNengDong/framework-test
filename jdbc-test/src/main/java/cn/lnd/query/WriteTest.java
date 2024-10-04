package cn.lnd.query;

import java.sql.*;

/**
 * @Author lnd
 * @Description
 * @Date 2024/9/29 21:40
 */
public class WriteTest {
    public static void main(String[] args) {
        try {
            // 建立连接
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mybatis_example", "root", "123456789");
            updateBatch(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateBatch(Connection conn) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement("update t_user set create_time = ? where id = ?");
            preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
            preparedStatement.setInt(2, 3);
            preparedStatement.addBatch();

            preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
            preparedStatement.setInt(2, 4);
            preparedStatement.addBatch();

            int[] result = preparedStatement.executeBatch();
            System.out.println("result = " + result.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
