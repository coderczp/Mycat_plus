/*
 * Copyright (c) 2018, MyCat_Plus and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */

package backup.io.mycat.handler.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

import io.mycat.backend.BackendConnection;
import io.mycat.backend.datasource.PhysicalDBNode;
import io.mycat.backend.mysql.nio.handler.ResponseHandler;
import io.mycat.config.ErrorCode;
import io.mycat.config.MycatConfig;
import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.TableConfig;
import io.mycat.net.BackendAIOConnection;
import io.mycat.net.FrontendConnection;
import io.mycat.net.mysql.CommandPacket;
import io.mycat.net.mysql.ErrorPacket;
import io.mycat.net.mysql.MySQLPacket;
import io.mycat.net.mysql.OkPacket;
import io.mycat.net.plus.ClientConn;
import io.mycat.route.RouteResultset;
import io.mycat.route.RouteResultsetNode;
import io.mycat.route.function.AbstractPartitionAlgorithm;
import io.mycat.route.util.RouterUtil;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.util.StringUtil;

/**
 * @author mycat
 */
public class ServerConnectionV2 extends FrontendConnection implements ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectionV2.class);
    private volatile byte       packetId;
    private volatile ByteBuffer buffer;
    private RouteResultsetNode  node;

    public ServerConnectionV2(NetworkChannel channel) throws IOException {
        super(channel);
    }

    public void execute(String sql, int type) {

        if (type != SQLHandler.Type.INSERT) {
            super.execute(sql, type);
            return;
        }

        //连接状态检查
        if (this.isClosed()) {
            LOGGER.warn("ignore execute ,front connection is closed {}", this);
            return;
        }
        // 事务状态检查
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback." + txInterrputMsg);
            return;
        }

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        if (stmtList.isEmpty()) {
            writeErrMessage(ErrorCode.ER_YES, "Empty sql");
            return;
        }

        for (SQLStatement sqlstmt : stmtList) {
            if (sqlstmt instanceof MySqlInsertStatement) {
                MySqlInsertStatement stmt = (MySqlInsertStatement) sqlstmt;
                processStatement(stmt, type, sql);
            } else {
                throw new RuntimeException("only suppport insert sql");
            }
        }
    }

    private void processStatement(MySqlInsertStatement insert, int type, String sql) {

        String table = insert.getTableName().getSimpleName().toUpperCase();
        //table: db.table
        if (table != null && table.contains("`")) {
            table = table.replaceAll("`", "");
        }

        String dbInSql = null;
        int dbIndex = table.indexOf(".");
        if (dbIndex > 0) {
            dbInSql = table.substring(dbIndex);
            table = table.substring(0, dbIndex);
        }

        String db = this.schema;
        if (db == null) {
            db = dbInSql;
        }

        // 检查当前使用的DB
        if (db == null) {
            writeErrMessage(ErrorCode.ERR_BAD_LOGICDB, "No MyCAT Database selected");
            return;
        }

        //检查所选的DB是否具有mycat对应的配置
        MycatConfig config = session.getCurrentConfig();
        SchemaConfig schema = config.getSchemas().get(db);
        if (schema == null) {
            writeErrMessage(ErrorCode.ERR_BAD_LOGICDB, "Unknown MyCAT Database '" + db + "'");
            return;
        }

        //不允许在SQL中切换数据库,垮库存在许多问题
        if (dbInSql != null && !dbInSql.equals(this.schema)) {
            writeErrMessage(ErrorCode.ERR_BAD_LOGICDB, "Does't support switching database in sql");
            return;
        }

        TableConfig tableConfig = schema.getTables().get(table);
        if (tableConfig == null) {
            writeErrMessage(ErrorCode.ER_NO_SUCH_TABLE, "No such MyCAT table '" + table + "'");
            return;
        }

        handleInsert(insert, sql, type, tableConfig);
    }

    /**
     * 
     * @param stmt
     * @param sql 
     * @param schema 
     * @param tblCfg 
     * @param type 
     */
    private void handleInsert(MySqlInsertStatement insert, String sql, int type, TableConfig tblCfg) {

        int partitionValueIndex = -1;
        List<SQLExpr> columns = insert.getColumns();
        String partitionColumn = tblCfg.getPartitionColumn();
        for (SQLExpr sqlExpr : columns) {
            partitionValueIndex++;
            if (sqlExpr.toString().equalsIgnoreCase(partitionColumn)) {
                break;
            }
        }

        if (partitionValueIndex == -1) {
            writeErrMessage(ErrorCode.ER_KEY_COLUMN_DOES_NOT_EXITS, "Partition column not found in insert sql");
            return;
        }

        AbstractPartitionAlgorithm function = tblCfg.getRule().getRuleAlgorithm();
        List<ValuesClause> valuesList = insert.getValuesList();
        if (valuesList.size() > 1) {
            //insert into xx values(),(),()需要拆分SQL
            System.out.println("----------------->");
        } else {

            SQLExpr sqlExpr = valuesList.get(0).getValues().get(partitionValueIndex);
            String partVal = sqlExpr.toString().replaceAll("'", "");

            Integer nodeIndex = function.calculate(partVal);
            RouteResultset rrs = new RouteResultset(sql, type);
            RouterUtil.routeToSingleNode(rrs, tblCfg.getDataNodes().get(nodeIndex), sql);

            node = rrs.getNodes()[0];
            this.executeSql = sql;
            BackendConnection conn = session.getTarget(node);
            if (session.tryExistsCon(conn, node)) {
                _execute(conn);
            } else {
                // create new connection
                MycatConfig conf = session.getCurrentConfig();
                PhysicalDBNode dn = conf.getDataNodes().get(node.getName());
                try {
                    dn.getConnection(dn.getDatabase(), isAutocommit(), node, this, node);
                } catch (Exception e) {
                    executeException(conn, e);
                }
            }
        }
    }

    private void executeException(BackendConnection c, Exception e) {
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;
        err.errno = ErrorCode.ERR_FOUND_EXCEPION;
        err.message = StringUtil.encode(e.toString(), session.getSource().getCharset());
        this.backConnectionErr(err, c);
    }

    private void backConnectionErr(ErrorPacket errPkg, BackendConnection conn) {
        ClientConn source = session.getSource();
        String errUser = source.getUser();
        String errHost = source.getHost();
        int errPort = source.getLocalPort();

        String errmgs = " errno:" + errPkg.errno + " " + new String(errPkg.message);
        LOGGER.warn("execute  sql err :" + errmgs + " con:" + conn + " frontend host:" + errHost + "/" + errPort + "/"
                    + errUser);
        session.releaseConnectionIfSafe(conn, LOGGER.isDebugEnabled(), false);
        source.setTxInterrupt(errmgs);
        errPkg.packetId = 1;
        errPkg.write(source);
        recycleResources();
    }

    private void recycleResources() {
        ByteBuffer buf = buffer;
        if (buf != null) {
            session.getSource().recycle(buffer);
            buffer = null;
        }
    }

    @Override
    public void connectionError(Throwable e, BackendConnection conn) {
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;
        err.errno = ErrorCode.ER_NEW_ABORTING_CONNECTION;
        err.message = StringUtil.encode(e.getMessage(), session.getSource().getCharset());
        ClientConn source = session.getSource();
        source.write(err.write(allocBuffer(), source, true));
    }

    /**
     * insert/update/delete
     * okResponse()：读取data字节数组，组成一个OKPacket，并调用ok.write(source)将结果写入前端连接FrontendConnection的写缓冲队列writeQueue中，
     * 真正发送给应用是由对应的NIOSocketWR从写队列中读取ByteBuffer并返回的
     */
    @Override
    public void okResponse(byte[] data, BackendConnection conn) {
        this.netOutBytes += data.length;
        ClientConn source = session.getSource();
        OkPacket ok = new OkPacket();
        ok.read(data);
        ok.packetId = ++packetId;// OK_PACKET
        ok.serverStatus = source.isAutocommit() ? 2 : 1;
        recycleResources();
        ok.write(source);
    }

    @Override
    public void rowEofResponse(byte[] eof, BackendConnection conn) {
        this.netOutBytes += eof.length;
        System.out.println("rowEofResponse:" + eof);
    }

    /**
     * lazy create ByteBuffer only when needed
     * 
     * @return
     */
    private ByteBuffer allocBuffer() {
        if (buffer == null) {
            buffer = session.getSource().allocate();
        }
        return buffer;
    }

    /**
     * select
     * 元数据返回时触发，将header和元数据内容依次写入缓冲区中
     */
    @Override
    public void fieldEofResponse(byte[] header, List<byte[]> fields, byte[] eof, BackendConnection conn) {
        System.out.println("fieldEofResponse" + header);
    }

    /**
     * select 
     * 
     * 行数据返回时触发，将行数据写入缓冲区中
     */
    @Override
    public void rowResponse(byte[] row, BackendConnection conn) {
        this.netOutBytes += row.length;
        System.out.println("rowResponse：" + row);
    }

    @Override
    public void writeQueueAvailable() {

    }

    @Override
    public void connectionClose(BackendConnection conn, String reason) {
        ErrorPacket err = new ErrorPacket();
        err.packetId = ++packetId;
        err.errno = ErrorCode.ER_ERROR_ON_CLOSE;
        err.message = StringUtil.encode(reason, session.getSource().getCharset());
        this.backConnectionErr(err, conn);

    }

    public void clearResources() {

    }

    private void _execute(BackendConnection conn) {
        if (session.closed()) {
            session.clearResources(true);
            return;
        }
        try {
            conn.setResponseHandler(this);
            CommandPacket packet = new CommandPacket();
            packet.command = MySQLPacket.COM_QUERY;
            packet.packetId = 0;
            packet.arg = executeSql.getBytes(charset);
            packet.write((BackendAIOConnection) conn);
        } catch (Exception e1) {
            executeException(conn, e1);
        }
    }

    @Override
    public void connectionAcquired(BackendConnection conn) {
        session.bindConnection(node, conn);
        _execute(conn);
    }

    @Override
    public void errorResponse(byte[] data, BackendConnection conn) {
        ErrorPacket err = new ErrorPacket();
        err.read(data);
        err.packetId = ++packetId;
        backConnectionErr(err, conn);
    }
}