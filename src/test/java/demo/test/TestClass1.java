package demo.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author mycat
 *
 */
public class TestClass1 {

    public static void main(String args[]) throws SQLException, ClassNotFoundException {
        String jdbcdriver = "com.mysql.jdbc.Driver";
        String session = "";//"&sessionVariables=$var={version:0.0.1}";
        String jdbcurl = "jdbc:mysql://127.0.0.1:8068/testdb?useUnicode=true&characterEncoding=utf-8" + session;
        String username = "root";
        String password = "root";
        System.out.println("开始连接mysql:" + jdbcurl);
        Class.forName(jdbcdriver);
        Connection c = DriverManager.getConnection(jdbcurl, username, password);
        c.setAutoCommit(false);
        PreparedStatement st = c.prepareStatement("insert into tpl (id,name) values(?,?)");
        long st1 = System.currentTimeMillis();
        for (int i = 1; i < 100000; i++) {
            st.setInt(1, i);
            st.setString(2, "555");
            st.addBatch();
            if (i % 100 == 0) {
                st.executeBatch();
                st.clearBatch();
            }
        }
        st.executeBatch();
        c.commit();
        st.close();
        c.close();
        long end = System.currentTimeMillis();
        System.out.println(end - st1);
        //30232 //44101 //51863 //31538
        //print( "test jdbc " , st.executeQuery("select count(*) from TPL ")); 
        // System.out.println("OK......");
    }

    static void print(String name, ResultSet res) throws SQLException {
        System.out.println(name);
        ResultSetMetaData meta = res.getMetaData();
        //System.out.println( "\t"+res.getRow()+"条记录");
        String str = "";
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            str += meta.getColumnName(i) + "   ";
            //System.out.println( meta.getColumnName(i)+"   ");
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
