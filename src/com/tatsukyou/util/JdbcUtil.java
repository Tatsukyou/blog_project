package com.tatsukyou.util;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class JdbcUtil {
    // 表示定义数据库的用户名
    private static String USERNAME;
    // 定义数据库的密码
    private static String PASSWORD;
    // 定义数据库的驱动信息
    private static String DRIVER;
    // 定义访问数据库的地址
    private static String URL;

    // 加载数据库配置信息，并给相关的属性赋值
    static {
        loadConfig();
    }

    // 定义数据库的链接
    private Connection connection;
    // 定义sql语句的执行对象
    private PreparedStatement pstmt;
    // 定义查询返回的结果集合
    private ResultSet resultSet;

    // 构造方法
    public JdbcUtil() {
    }

    /**
     * 加载数据库配置信息，并给相关的属性赋值
     */
    public static void loadConfig() {
        try {
            // 从类路径中获取数据库配置文件
            InputStream inStream = JdbcUtil.class.getResourceAsStream("/jdbc.properties");
            Properties prop = new Properties();
            prop.load(inStream);
            // 读取属性并赋值
            USERNAME = prop.getProperty("jdbc.username");
            PASSWORD = prop.getProperty("jdbc.password");
            DRIVER = prop.getProperty("jdbc.driver");
            URL = prop.getProperty("jdbc.url");
        } catch (Exception e) {
            throw new RuntimeException("读取数据库配置文件异常！", e);
        }
    }

    public static void main(String[] args) {
        // 创建JdbcUtil对象
        JdbcUtil jdbcUtil = new JdbcUtil();
        // 获取数据库连接
        jdbcUtil.getConnection();
        try {
            // 执行查询操作
            List<Map<String, Object>> result = jdbcUtil.findResult(
                    "select * from article", null);
            // 输出查询结果
            for (Map<String, Object> m : result) {
                System.out.println(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            jdbcUtil.releaseConn();
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接
     */
    public Connection getConnection() {
        try {
            // 注册驱动
            Class.forName(DRIVER);
            // 获取连接
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("获取数据库连接异常！", e);
        }
        return connection;
    }

    /**
     * 执行更新操作
     * @param sql    sql语句
     * @param params 执行参数
     * @return 执行结果
     * @throws SQLException
     */
    public boolean updateByPreparedStatement(String sql, List<?> params)
            throws SQLException {
        boolean flag = false;
        int result = -1; // 表示当用户执行添加删除和修改的时候所影响数据库的行数
        pstmt = connection.prepareStatement(sql);
        int index = 1;
        // 填充sql语句中的占位符
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        // 执行更新操作
        result = pstmt.executeUpdate();
        flag = result > 0;
        return flag;
    }

    /**
     * 释放资源
     */
    public void releaseConn() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行查询操作
     * @param sql    sql语句
     * @param params 执行参数
     * @return 查询结果
     * @throws SQLException
     */
    public List<Map<String, Object>> findResult(String sql, List<?> params)
            throws SQLException {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int index = 1;
        pstmt = connection.prepareStatement(sql);
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(index++, params.get(i));
            }
        }
        // 执行查询操作
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        // 处理查询结果
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < cols_len; i++) {
                String cols_name = metaData.getColumnName(i + 1);
                Object cols_value = resultSet.getObject(cols_name);
                if (cols_value == null) {
                    cols_value = "";
                }
                map.put(cols_name, cols_value);
            }
            list.add(map);
        }
        return list;
    }
}
