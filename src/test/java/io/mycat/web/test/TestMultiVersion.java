package io.mycat.web.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author mycat
 *
 */
public class TestMultiVersion {

    public static void main(String args[]) throws SQLException, ClassNotFoundException {
        String jdbcdriver = "com.mysql.jdbc.Driver";
        String jdbcurl = "jdbc:mysql://127.0.0.1:8068/testdb?useUnicode=true&characterEncoding=utf-8&sessionVariables=$var={version:default}";
        String username = "root";
        String password = "root";
        System.out.println("开始连接mysql:" + jdbcurl);
        Class.forName(jdbcdriver);
        Connection c = DriverManager.getConnection(jdbcurl, username, password);
        Statement st = c.createStatement();
        print("test jdbc ", st.executeQuery("select *  from tpl "));
        System.out.println("OK......");
        c.close();
    }

    static void print(String name, ResultSet res) throws SQLException {
        System.out.println(name);
        ResultSetMetaData meta = res.getMetaData();
        String str = "";
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            str += meta.getColumnName(i) + "   ";
        }
        System.out.println("\t" + str);
        str = "";
        while (res.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                str += res.getString(i) + "   ";
            }
            System.out.println("\t" + str);
            str = "";
        }
    }
}
