package main.java.com.test;
import java.sql.*;

public class sql {

    public static void main(String[] args) {
        try {
            // 1. 加载JDBC驱动程序
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. 建立与数据库的连接
            String url = "jdbc:mysql://localhost:3306/mydb";
            String username = "root";
            String password = "123456";
            Connection conn = DriverManager.getConnection(url, username, password);

            // 3. 创建Statement对象并执行SQL查询语句
            String sql1 = "SELECT * FROM users WHERE age > ?";
            String sql2 = "SELECT * FROM users WHERE age > ?";
            String sql3 = "SELECT * FROM users WHERE age > ?";
            String insertSql = "SELECT * FROM users WHERE age > ?";
            PreparedStatement statement = conn.prepareStatement(sql1);
            statement.setInt(1, 18); // 设置参数值
            ResultSet resultSet = statement.executeQuery();
            int rows = statement.executeUpdate(insertSql);
            // 4. 遍历查询结果并提取数据
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                System.out.println("id=" + id + ", name=" + name + ", age=" + age);
            }

            // 5. 关闭资源
            resultSet.close();
            statement.close();
            conn.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}