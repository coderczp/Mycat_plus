package io.mycat.server.handler.plus.impl.ext;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import io.mycat.MycatServer;
import io.mycat.backend.mysql.PacketUtil;
import io.mycat.config.Fields;
import io.mycat.config.model.SchemaConfig;
import io.mycat.net.mysql.EOFPacket;
import io.mycat.net.mysql.FieldPacket;
import io.mycat.net.mysql.ResultSetHeaderPacket;
import io.mycat.net.mysql.RowDataPacket;
import io.mycat.net.plus.ClientConn;
import io.mycat.server.Session;
import io.mycat.server.SqlContext;
import io.mycat.server.response.InformationSchemaProfiling;
import io.mycat.util.LongUtil;
import io.mycat.util.StringUtil;

/**
 * 对 PhpAdmin's 控制台操作进行支持 
 * 如：SELECT * FROM information_schema.CHARACTER_SETS 等相关语句进行模拟返回
 * 
 * @author zhuam
 *
 */
public class MysqlInformationSchemaHandler {

    static class Result {
        FieldPacket[]   fields;
        RowDataPacket[] rows;
    }

    private static void doWrite(Result rs, ClientConn c) {
        ByteBuffer buffer = c.allocate();

        // write header
        ResultSetHeaderPacket header = PacketUtil.getHeader(rs.fields.length);
        byte packetId = header.packetId;
        buffer = header.write(buffer, c, true);

        // write fields
        for (FieldPacket field : rs.fields) {
            field.packetId = ++packetId;
            buffer = field.write(buffer, c, true);
        }

        // write eof
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.write(buffer, c, true);

        // write rows
        for (RowDataPacket row : rs.rows) {
            row.packetId = ++packetId;
            buffer = row.write(buffer, c, true);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.write(buffer, c, true);

        // post write
        c.write(buffer);
    }

    /**
     * 兼容PhpAdmin's, 支持对MySQL元数据的模拟返回
                     支持更多information_schema特性
        MySQL Front这类工具连接上了就会执行如下语句,需要路由在mysql执行
        SELECT SYSDATE(),CURRENT_USER();
        SHOW GRANTS FOR CURRENT_USER();
        HELP 'SELECT';
        HELP 'VERSION';
        SELECT * FROM `INFORMATION_SCHEMA`.`SESSION_VARIABLES`;
        SELECT * FROM `INFORMATION_SCHEMA`.`ENGINES`;
        SELECT * FROM `INFORMATION_SCHEMA`.`CHARACTER_SETS`;
        SELECT * FROM `INFORMATION_SCHEMA`.`COLLATIONS`;
        SELECT * FROM `INFORMATION_SCHEMA`.`SCHEMATA`;
     ***/
    public static void handle(String sql, ClientConn c) {

        Session session2 = c.getSession2();
        SqlContext ast = session2.getSqlCtx();

        //fix navicat   SELECT STATE AS `State`, ROUND(SUM(DURATION),7) AS `Duration`, CONCAT(ROUND(SUM(DURATION)/*100,3), '%') AS `Percentage` FROM INFORMATION_SCHEMA.PROFILING WHERE QUERY_ID= GROUP BY STATE ORDER BY SEQ
        if (sql.contains("INFORMATION_SCHEMA.PROFILING ") && sql.contains("CONCAT(ROUND(SUM(DURATION)/")) {
            InformationSchemaProfiling.response(c);
            return;
        }

        String item = sql;
        String charset = c.getCharset();
        if (item.equals("SELECT SYSDATE(),CURRENT_USER()")) {
            int fieldCount = 2;
            FieldPacket[] fields = new FieldPacket[fieldCount];
            fields[0] = PacketUtil.getField("SYSDATE()", Fields.FIELD_TYPE_TIMESTAMP);
            fields[1] = PacketUtil.getField("CURRENT_USER()", Fields.FIELD_TYPE_VAR_STRING);

            RowDataPacket[] rows = new RowDataPacket[1];
            RowDataPacket row = new RowDataPacket(fieldCount);
            row.add(LongUtil.toBytes(System.currentTimeMillis()));
            row.add(StringUtil.encode("mycat", charset));
            rows[0] = row;

            Result rs = new Result();
            rs.fields = fields;
            rs.rows = rows;
            doWrite(rs, c);
        } else if (item.equals("SHOW GRANTS FOR CURRENT_USER()")) {
            int fieldCount = 1;
            FieldPacket[] fields = new FieldPacket[fieldCount];

            fields[0] = PacketUtil.getField("GRANTS for mycat@localhost", Fields.FIELD_TYPE_VAR_STRING);

            RowDataPacket[] rows = new RowDataPacket[1];
            RowDataPacket row = new RowDataPacket(fieldCount);
            row.add(StringUtil.encode("GRANT PROXY ON ''@'' TO 'mycat'@'localhost' WITH GRANT OPTION", charset));
            rows[0] = row;

            Result rs = new Result();
            rs.fields = fields;
            rs.rows = rows;
            doWrite(rs, c);
        } else {
            String table = ast.getVistor().getTableWithOutDB().toUpperCase();
            if (table.equals("CHARACTER_SETS")) {
                queryCharacterSets(c, charset);
            } else if (table.equals("SESSION_VARIABLES")) {
                querySessionVariables(c, charset);
            } else if (table.equals("ENGINES")) {
                queryEngines(c, charset);
            } else if (table.equals("COLLATIONS")) {
                queryCollations(c, charset);
            } else if (table.equals("SCHEMATA")) {
                querySchema(c, charset);
            }
        }

    }

    /**
     * 
     * @param c
     * @param charset
     */
    private static void queryCharacterSets(ClientConn c, String charset) {
        int field_count = 4;
        FieldPacket[] fields = new FieldPacket[field_count];
        fields[0] = PacketUtil.getField("CHARACTER_SET_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("DEFAULT_COLLATE_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[2] = PacketUtil.getField("DESCRIPTION", Fields.FIELD_TYPE_VAR_STRING);
        fields[3] = PacketUtil.getField("MAXLEN", Fields.FIELD_TYPE_LONG);

        RowDataPacket[] rows = new RowDataPacket[1];
        RowDataPacket row = new RowDataPacket(field_count);
        row.add(StringUtil.encode("big5", charset));
        row.add(StringUtil.encode("big5_chinese_ci", charset));
        row.add(StringUtil.encode("Big5 Traditional Chinese", charset));
        row.add(LongUtil.toBytes(2));
        rows[0] = row;

        Result rs = new Result();
        rs.fields = fields;
        rs.rows = rows;
        doWrite(rs, c);
    }

    /**
     * 
     * @param c
     * @param charset
     */
    private static void querySessionVariables(ClientConn c, String charset) {
        int field_count = 2;
        FieldPacket[] fields = new FieldPacket[field_count];
        fields[0] = PacketUtil.getField("VARIABLE_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("VARIABLE_VALUE", Fields.FIELD_TYPE_VAR_STRING);
        
        RowDataPacket row = new RowDataPacket(field_count);
        row.add(StringUtil.encode("TIME_ZONE", "SYSTEM"));
        row.add(StringUtil.encode("BINLOG_FORMAT", "ROW"));
        
        RowDataPacket[] rows = new RowDataPacket[] {row};
        
        Result rs = new Result();
        rs.fields = fields;
        rs.rows = rows;
        doWrite(rs, c);
    }

    /**
     * 
     * @param c
     * @param charset
     */
    private static void queryEngines(ClientConn c, String charset) {
        int field_count = 6;
        FieldPacket[] fields = new FieldPacket[field_count];
        fields[0] = PacketUtil.getField("ENGINE", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("SUPPORT", Fields.FIELD_TYPE_VAR_STRING);
        fields[2] = PacketUtil.getField("COMMENT", Fields.FIELD_TYPE_VAR_STRING);
        fields[3] = PacketUtil.getField("TRANSITIONS", Fields.FIELD_TYPE_VAR_STRING);
        fields[4] = PacketUtil.getField("XA", Fields.FIELD_TYPE_VAR_STRING);
        fields[5] = PacketUtil.getField("SAVEPOINTS", Fields.FIELD_TYPE_VAR_STRING);

        RowDataPacket[] rows = new RowDataPacket[1];
        RowDataPacket row = new RowDataPacket(field_count);
        row.add(StringUtil.encode("InnoDB", charset));
        row.add(StringUtil.encode("default ", charset));
        row.add(StringUtil.encode("Supports transactions, row-level locking, and foreign keys ", charset));
        row.add(StringUtil.encode("YES ", charset));
        row.add(StringUtil.encode("YES ", charset));
        row.add(StringUtil.encode("YES", charset));
        rows[0] = row;

        Result rs = new Result();
        rs.fields = fields;
        rs.rows = rows;
        doWrite(rs, c);
    }

    /**
     * 
     * @param c
     * @param charset
     */
    private static void queryCollations(ClientConn c, String charset) {
        int field_count = 6;
        FieldPacket[] fields = new FieldPacket[field_count];
        fields[0] = PacketUtil.getField("COLLATION_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("CHARACTER_SET_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[2] = PacketUtil.getField("ID", Fields.FIELD_TYPE_VAR_STRING);
        fields[3] = PacketUtil.getField("IS_DEFAULT", Fields.FIELD_TYPE_VAR_STRING);
        fields[4] = PacketUtil.getField("IS_COMPILED", Fields.FIELD_TYPE_VAR_STRING);
        fields[5] = PacketUtil.getField("SORTLEN", Fields.FIELD_TYPE_VAR_STRING);

        RowDataPacket[] rows = new RowDataPacket[1];
        RowDataPacket row = new RowDataPacket(field_count);
        row.add(StringUtil.encode("utf8_bin", charset));
        row.add(StringUtil.encode("utf8 ", charset));
        row.add(StringUtil.encode("196", charset));
        row.add(StringUtil.encode("", charset));
        row.add(StringUtil.encode("YES ", charset));
        row.add(StringUtil.encode("8", charset));
        rows[0] = row;

        Result rs = new Result();
        rs.fields = fields;
        rs.rows = rows;
        doWrite(rs, c);
    }

    /**
     * 
     * @param c
     * @param charset
     */
    private static void querySchema(ClientConn c, String charset) {
        int field_count = 5;
        FieldPacket[] fields = new FieldPacket[field_count];
        fields[0] = PacketUtil.getField("CATALOG_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("SCHEMA_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[2] = PacketUtil.getField("DEFAULT_CHARACTER_SET_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[3] = PacketUtil.getField("DEFAULT_COLLATION_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[4] = PacketUtil.getField("SQL_PATH", Fields.FIELD_TYPE_VAR_STRING);

        Map<String, SchemaConfig> schemas = MycatServer.getInstance().getConfig().getSchemas();
        RowDataPacket[] rows = new RowDataPacket[schemas.size()];
        int i = 0;
        for (Entry<String, SchemaConfig> sc : schemas.entrySet()) {
            RowDataPacket row = new RowDataPacket(field_count);
            row.add(StringUtil.encode("def", charset));
            row.add(StringUtil.encode("utf-8", charset));
            row.add(StringUtil.encode(sc.getKey(), charset));
            row.add(StringUtil.encode("utf8_general_ci", charset));
            row.add(StringUtil.encode("", charset));
            rows[i++] = row;

        }
        Result rs = new Result();
        rs.fields = fields;
        rs.rows = rows;
        doWrite(rs, c);
    }
}