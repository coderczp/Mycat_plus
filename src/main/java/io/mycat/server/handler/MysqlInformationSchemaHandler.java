package io.mycat.server.handler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import io.mycat.MycatServer;
import io.mycat.backend.mysql.PacketUtil;
import io.mycat.config.Fields;
import io.mycat.config.model.SchemaConfig;
import io.mycat.net.mysql.EOFPacket;
import io.mycat.net.mysql.FieldPacket;
import io.mycat.net.mysql.OkPacket;
import io.mycat.net.mysql.ResultSetHeaderPacket;
import io.mycat.net.mysql.RowDataPacket;
import io.mycat.server.ServerConnection;
import io.mycat.server.util.SchemaUtil;
import io.mycat.util.LongUtil;
import io.mycat.util.StringUtil;

/**
 * 对 PhpAdmin's 控制台操作进行支持 
 * 
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

    private static void doWrite(Result rs, ServerConnection c) {
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
    public static void handle(String sql, ServerConnection c) {
        String charset = c.getCharset();
        String[] sqls = sql.split(";");
        for (String item : sqls) {
            item = item.trim();
            if (item.isEmpty())
                continue;

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

            } else if (item.equals("HELP 'SELECT'")) {
                int fieldCount = 3;
                FieldPacket[] fields = new FieldPacket[fieldCount];
                fields[0] = PacketUtil.getField("NAME", Fields.FIELD_TYPE_VAR_STRING);
                fields[1] = PacketUtil.getField("DESCRIPTION", Fields.FIELD_TYPE_VAR_STRING);
                fields[2] = PacketUtil.getField("EXAMPLE", Fields.FIELD_TYPE_VAR_STRING);

                RowDataPacket[] rows = new RowDataPacket[1];
                RowDataPacket row = new RowDataPacket(fieldCount);
                row.add(StringUtil.encode("select", charset));
                row.add(StringUtil.encode("select xx from ", charset));
                row.add(StringUtil.encode("", charset));
                rows[0] = row;

                Result rs = new Result();
                rs.fields = fields;
                rs.rows = rows;
                doWrite(rs, c);
            } else if (item.equals("HELP 'VERSION'")) {
                int fieldCount = 3;
                FieldPacket[] fields = new FieldPacket[fieldCount];
                fields[0] = PacketUtil.getField("name", Fields.FIELD_TYPE_VAR_STRING);
                fields[1] = PacketUtil.getField("description", Fields.FIELD_TYPE_VAR_STRING);
                fields[2] = PacketUtil.getField("example", Fields.FIELD_TYPE_VAR_STRING);

                RowDataPacket[] rows = new RowDataPacket[1];
                RowDataPacket row = new RowDataPacket(fieldCount);
                row.add(StringUtil.encode("select", charset));
                row.add(StringUtil.encode("select xx from ", charset));
                row.add(StringUtil.encode("", charset));
                rows[0] = row;

                Result rs = new Result();
                rs.fields = fields;
                rs.rows = rows;
                doWrite(rs, c);

            } else {
                SchemaUtil.SchemaInfo schemaInfo = SchemaUtil.parseSchema(item);
                if (schemaInfo != null) {
                    String table = schemaInfo.table.toUpperCase();
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

        }
        if (sqls.length == 0) {
            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        }
    }

    /**
     * 
     * @param c
     * @param charset
     */
    private static void queryCharacterSets(ServerConnection c, String charset) {
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
    private static void querySessionVariables(ServerConnection c, String charset) {
        int field_count = 2;
        FieldPacket[] fields = new FieldPacket[field_count];
        fields[0] = PacketUtil.getField("VARIABLE_NAME", Fields.FIELD_TYPE_VAR_STRING);
        fields[1] = PacketUtil.getField("VARIABLE_VALUE", Fields.FIELD_TYPE_VAR_STRING);

        RowDataPacket[] rows = new RowDataPacket[1];
        RowDataPacket row = new RowDataPacket(field_count);
        row.add(StringUtil.encode("TIME_ZONE", charset));
        row.add(StringUtil.encode("system ", charset));
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
    private static void queryEngines(ServerConnection c, String charset) {
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
    private static void queryCollations(ServerConnection c, String charset) {
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
    private static void querySchema(ServerConnection c, String charset) {
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