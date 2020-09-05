package com.cxwmpt.demo.common.util;

import java.sql.*;

/**
 * JDBC 工具类
 */
public class JDBCUtils {
    private static String url;
    private static String user;
    private static String password;
    private  static String driver;

    /**
     * 文件读取，只会执行一次，使用静态代码块
     */
    static {
        //读取文件，获取值
        try {
            System.out.println("注册驱动JDBC------");
            //3获取数据
            url = "jdbc:mysql://106.12.21.25:3306/db_sft?userSSL=false&useUnicode=true&characterEncoding=utf8&autoReconnect=true&serverTimezone=GMT%2B8";
            user = "root";
            password = "system2016";
            driver ="com.mysql.cj.jdbc.Driver";
            //4.注册驱动
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("注册数据库驱动出现异常:"+e.getMessage());
        }
    }
    /**
     * 获取连接
     * @return 连接对象
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
        } catch (SQLException ex1) {
            throw new RuntimeException("无法获取连接,原因:"+ex1.getMessage());
        }
    }
    //创建执行insert、update、delete的方法（只执行一条sql静态命令），返回受影响的行的个数。
    public static int UpdateSql(String sql){
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        int result=0;
        try {
            conn = JDBCUtils.getConnection();
            //执行sql
            st = conn.createStatement();
            result = st.executeUpdate(sql);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(rs, st, conn);
        }
        return result;
    }


    /**
     * 释放资源
     * @param rs
     * @param st
     * @param conn
     */
    public static void close(ResultSet rs, Statement st, Connection conn){
        if (rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(st != null){
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
