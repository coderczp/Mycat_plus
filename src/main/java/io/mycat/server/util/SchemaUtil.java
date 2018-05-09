package io.mycat.server.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

import io.mycat.config.model.SchemaConfig;
import io.mycat.route.parser.druid.MycatSchemaStatVisitor;
import io.mycat.server.Session;
import io.mycat.server.handler.plus.SQLHandler;

/**
 * Created by magicdoom on 2016/1/26.
 */
public class SchemaUtil {


    public static String detectDefaultDb(String sql, int type, Session session) {

        MycatSchemaStatVisitor vistor = session.getSqlCtx().getVistor();
        Map<String, SchemaConfig> schemas = session.getCurrentConfig().getSchemas();

        if (SQLHandler.Type.SELECT == type && vistor.getCurrentTable() == null) {
            //返回默认数据库
            return schemas.entrySet().iterator().next().getKey();
        }

        if ((SQLHandler.Type.SHOW == type || SQLHandler.Type.USE == type || SQLHandler.Type.EXPLAIN == type
             || SQLHandler.Type.SET == type || SQLHandler.Type.HELP == type || SQLHandler.Type.DESCRIBE == type)
            && !schemas.isEmpty()) {
            //兼容mysql gui  不填默认database
            return schemas.entrySet().iterator().next().getKey();
        }

        return vistor.getSchema();
        /***
        if (SQLHandler.Type.SELECT == type) {
            SchemaUtil.SchemaInfo schemaInfo = SchemaUtil.parseSchema(sql);
            db = schemaInfo.schema;
        
            if (schemaInfo.table == null && !schemas.isEmpty()) {
                db = schemas.entrySet().iterator().next().getKey();
            }
                if (schemaConfigMap.containsKey(schemaInfo.schema)) {
                    db = schemaInfo.schema;
                } else if ("information_schema".equalsIgnoreCase(schemaInfo.schema)) {
                    db = "information_schema";
                }
            }
        } else if (SQLHandler.Type.INSERT == type || SQLHandler.Type.UPDATE == type || SQLHandler.Type.DELETE == type
                   || SQLHandler.Type.DDL == type) {
            SchemaUtil.SchemaInfo schemaInfo = SchemaUtil.parseSchema(sql);
            if (schemaInfo != null && schemaInfo.schema != null && schemas.containsKey(schemaInfo.schema)) {
                db = schemaInfo.schema;
            }
        } else if ((SQLHandler.Type.SHOW == type || SQLHandler.Type.USE == type || SQLHandler.Type.EXPLAIN == type
                    || SQLHandler.Type.SET == type || SQLHandler.Type.HELP == type || SQLHandler.Type.DESCRIBE == type)
                   && !schemas.isEmpty()) {
            //兼容mysql gui  不填默认database
            db = schemas.entrySet().iterator().next().getKey();
        }
        return db;
        ***/
    }

    public static String parseShowTableSchema(String sql) {
        Matcher ma = pattern.matcher(sql);
        if (ma.matches() && ma.groupCount() >= 5) {
            return ma.group(5);
        }
        return null;
    }

    private static SchemaInfo parseSchema(String sql) {
        SQLStatementParser parser = new MySqlStatementParser(sql);
        return parseTables(parser.parseStatement(), new MycatSchemaStatVisitor());
    }

    private static SchemaInfo parseTables(SQLStatement stmt, SchemaStatVisitor vistor) {
        String table = null;
        SchemaInfo schemaInfo = new SchemaInfo();
        if (stmt instanceof MySqlInsertStatement) {
            table = ((MySqlInsertStatement) stmt).getTableName().getSimpleName();
        } else {
            stmt.accept(vistor);
            table = vistor.getCurrentTable();
        }
        if (table != null) {
            table = table.replaceAll("`", "");
        } else {
            //select 1 ;没有table和db
            return schemaInfo;
        }

        int pos = table.indexOf(".");
        if (pos > 0) {
            schemaInfo.schema = table.substring(0, pos);
            schemaInfo.table = table.substring(pos + 1);
        } else {
            schemaInfo.table = table;
        }
        return schemaInfo;

    }

    public static class SchemaInfo {
        public String table;
        public String schema;

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("SchemaInfo{");
            sb.append("table='").append(table).append('\'');
            sb.append(", schema='").append(schema).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    private static Pattern pattern = Pattern.compile(
        "^\\s*(SHOW)\\s+(FULL)*\\s*(TABLES)\\s+(FROM)\\s+([a-zA-Z_0-9]+)\\s*([a-zA-Z_0-9\\s]*)",
        Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) {
        String sql = "SELECT name, type FROM `mysql`.`proc` as xxxx WHERE Db='base'";
        //   System.out.println(parseSchema(sql));
        sql = "insert into aaa.test(id) values(1)";
        // System.out.println(parseSchema(sql));
        sql = "update updatebase.test set xx=1 ";
        //System.out.println(parseSchema(sql));
        sql = "CREATE TABLE IF not EXISTS  `test` (\n" + "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n"
              + "  `sid` bigint(20) DEFAULT NULL,\n" + "  `name` varchar(45) DEFAULT NULL,\n"
              + "  `value` varchar(45) DEFAULT NULL,\n" + "  `_slot` int(11) DEFAULT NULL COMMENT '自动迁移算法slot,禁止修改',\n"
              + "  PRIMARY KEY (`id`)\n" + ") ENGINE=InnoDB AUTO_INCREMENT=805781256930734081 DEFAULT CHARSET=utf8";
        System.out.println(parseSchema(sql));
        String pat3 = "show  full  tables from  base like ";
        Matcher ma = pattern.matcher(pat3);
        if (ma.matches()) {
            System.out.println(ma.groupCount());
            System.out.println(ma.group(5));
        }

    }
}
