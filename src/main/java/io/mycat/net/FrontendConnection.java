/*
 * Copyright (c) 2013, MyCat_Plus and/or its affiliates. All rights reserved.
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
package io.mycat.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backup.io.mycat.server.handler.MysqlProcHandler;
import io.mycat.MycatServer;
import io.mycat.backend.mysql.CharsetUtil;
import io.mycat.backend.mysql.MySQLMessage;
import io.mycat.buffer.BufferPool;
import io.mycat.config.Capabilities;
import io.mycat.config.ErrorCode;
import io.mycat.config.MycatConfig;
import io.mycat.config.Versions;
import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.net.handler.FrontendAuthenticator;
import io.mycat.net.handler.FrontendPrepareHandler;
import io.mycat.net.handler.FrontendPrivileges;
import io.mycat.net.handler.FrontendQueryHandler;
import io.mycat.net.handler.LoadDataInfileProcessor;
import io.mycat.net.mysql.ErrorPacket;
import io.mycat.net.mysql.HandshakePacket;
import io.mycat.net.mysql.HandshakeV10Packet;
import io.mycat.net.mysql.MySQLPacket;
import io.mycat.net.mysql.OkPacket;
import io.mycat.net.plus.ClientConn;
import io.mycat.route.RouteResultset;
import io.mycat.route.RouteService;
import io.mycat.server.NonBlockingSession;
import io.mycat.server.Session;
import io.mycat.server.SqlContext;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.handler.plus.impl.ext.MysqlInformationSchemaHandler;
import io.mycat.server.parser.plus.StatementHolder;
import io.mycat.server.parser.plus.SqlAstParser;
import io.mycat.server.response.Ping;
import io.mycat.server.util.SchemaUtil;
import io.mycat.statistic.CommandCount;
import io.mycat.util.CompressUtil;
import io.mycat.util.RandomUtil;
import io.mycat.util.SplitUtil;
import io.mycat.util.TimeUtil;

/**
 * @author mycat
 */
public class FrontendConnection extends AbstractConnection implements ClientConn {

    private static final Logger       LOGGER         = LoggerFactory.getLogger(FrontendConnection.class);

    protected long                    id;
    protected String                  host;
    protected int                     port;
    protected int                     localPort;
    protected long                    idleTimeout;
    protected byte[]                  seed;
    protected String                  user;
    protected String                  schema;
    protected String                  executeSql;

    protected volatile boolean        autocommit;
    protected volatile boolean        txInterrupted;
    protected volatile int            txIsolation;
    protected volatile String         txInterrputMsg = "";

    protected FrontendPrivileges      privileges;
    protected FrontendQueryHandler    queryHandler;
    protected FrontendPrepareHandler  prepareHandler;
    protected LoadDataInfileProcessor loadDataInfileHandler;
    protected boolean                 isAuthenticated;
    //protected boolean                isAccepted;

    protected static final long       AUTH_TIMEOUT   = 15 * 1000L;
    protected volatile boolean        preAcStates;                                                       //上一个ac状态,默认为true
    protected long                    lastInsertId;
    protected NonBlockingSession      session;
    protected RouteService            router;
    protected SystemConfig            system;

    /**
     * 标志是否执行了lock tables语句，并处于lock状态
     */
    protected volatile boolean        isLocked       = false;

    public FrontendConnection(NetworkChannel channel) throws IOException {
        super(channel);
        InetSocketAddress localAddr = (InetSocketAddress) channel.getLocalAddress();
        InetSocketAddress remoteAddr = null;
        if (channel instanceof SocketChannel) {
            remoteAddr = (InetSocketAddress) ((SocketChannel) channel).getRemoteAddress();

        } else if (channel instanceof AsynchronousSocketChannel) {
            remoteAddr = (InetSocketAddress) ((AsynchronousSocketChannel) channel).getRemoteAddress();
        }

        this.router = MycatServer.getInstance().getRouterService();
        this.host = remoteAddr.getHostString();
        this.port = localAddr.getPort();
        this.localPort = remoteAddr.getPort();
        this.handler = new FrontendAuthenticator(this);
        this.autocommit = true;
        this.txInterrupted = false;
        this.preAcStates = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void setProcessor(NIOProcessor processor) {
        super.setProcessor(processor);
        processor.addFrontend(this);
    }

    public LoadDataInfileProcessor getLoadDataInfileHandler() {
        return loadDataInfileHandler;
    }

    public void setLoadDataInfileHandler(LoadDataInfileProcessor loadDataInfileHandler) {
        this.loadDataInfileHandler = loadDataInfileHandler;
    }

    public void setQueryHandler(FrontendQueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    public void setPrepareHandler(FrontendPrepareHandler prepareHandler) {
        this.prepareHandler = prepareHandler;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public FrontendPrivileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(FrontendPrivileges privileges) {
        this.privileges = privileges;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getExecuteSql() {
        return executeSql;
    }

    public void setExecuteSql(String executeSql) {
        this.executeSql = executeSql;
    }

    public byte[] getSeed() {
        return seed;
    }

    public boolean setCharsetIndex(int ci) {
        String charset = CharsetUtil.getCharset(ci);
        if (charset != null) {
            return setCharset(charset);
        } else {
            return false;
        }
    }

    public int getTxIsolation() {
        return txIsolation;
    }

    public void setTxIsolation(int txIsolation) {
        this.txIsolation = txIsolation;
    }

    public void writeErrMessage(int errno, String msg) {
        writeErrMessage((byte) 1, errno, msg);
    }

    public void writeErrMessage(byte id, int errno, String msg) {
        ErrorPacket err = new ErrorPacket();
        err.message = encodeString(msg, charset);
        err.packetId = id;
        err.errno = errno;
        err.write(this);
    }

    public void initDB(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        mm.position(5);
        String db = mm.readString();

        // 检查schema的有效性
        if (db == null || !privileges.schemaExists(db)) {
            writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return;
        }

        if (!privileges.userExists(user, host)) {
            writeErrMessage(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + user + "'");
            return;
        }

        Set<String> schemas = privileges.getUserSchemas(user);
        if (schemas == null || schemas.size() == 0 || schemas.contains(db)) {
            this.schema = db;
            write(writeToBuffer(OkPacket.OK, allocate()));
        } else {
            String s = "Access denied for user '" + user + "' to database '" + db + "'";
            writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, s);
        }
    }

    public void query(String sql) {

        if (sql == null || sql.length() == 0) {
            writeErrMessage(ErrorCode.ER_NOT_ALLOWED_COMMAND, "Empty SQL");
            return;
        }

        //产生SQLAST并处理
        SqlContext sqlCtx = session.getSqlCtx();
        SqlAstParser parser = new SqlAstParser(sql, true);
        List<StatementHolder> stmts = parser.parseStatements();
        for (StatementHolder stmt : stmts) {
            sqlCtx.setStmt(stmt);
            processOneSql(sqlCtx);
        }

        if (stmts.isEmpty()) {
            LOGGER.error("parse sql return empty,sql:{}", sql);
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Empty Sql");
        }
    }

    private void processOneSql(SqlContext ast) {

        String sql = ast.getSql();
        setExecuteSql(sql);

        FrontendPrivileges privileges = this.privileges;
        // 防火墙策略( SQL 黑名单/ 注入攻击)
        if (!privileges.checkFirewallSQLPolicy(user, sql)) {
            writeErrMessage(ErrorCode.ERR_WRONG_USED, "Unsafe SQL, reject for user '" + user + "'");
            return;
        }

        // DML 权限检查
        try {
            if (!privileges.checkDmlPrivilege(user, schema, session)) {
                writeErrMessage(ErrorCode.ERR_WRONG_USED, "Privilege reject");
                return;
            }
        } catch (com.alibaba.druid.sql.parser.ParserException e1) {
            writeErrMessage(ErrorCode.ERR_WRONG_USED, e1.getMessage());
            LOGGER.error("parse exception", e1);
            return;
        }

        // 执行查询
        if (queryHandler != null) {
            queryHandler.setReadOnly(privileges.isReadOnly(user));
            queryHandler.query(sql);
            return;
        }

        LOGGER.error("queryHandler is null,for:{}", sql);
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Query unsupported!");
    }

    public void query(byte[] data) {
        try {
            MySQLMessage mm = new MySQLMessage(data);
            mm.position(5);
            String sql = mm.readString(charset);
            this.query(sql);
        } catch (UnsupportedEncodingException e) {
            writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
            return;
        }
    }

    public void stmtPrepare(byte[] data) {
        if (prepareHandler != null) {
            // 取得语句
            MySQLMessage mm = new MySQLMessage(data);
            mm.position(5);
            String sql = null;
            try {
                sql = mm.readString(charset);
            } catch (UnsupportedEncodingException e) {
                writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
                return;
            }
            if (sql == null || sql.length() == 0) {
                writeErrMessage(ErrorCode.ER_NOT_ALLOWED_COMMAND, "Empty SQL");
                return;
            }

            // 记录SQL
            this.setExecuteSql(sql);

            // 执行预处理
            prepareHandler.prepare(sql);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void stmtSendLongData(byte[] data) {
        if (prepareHandler != null) {
            prepareHandler.sendLongData(data);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void stmtReset(byte[] data) {
        if (prepareHandler != null) {
            prepareHandler.reset(data);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void stmtExecute(byte[] data) {
        if (prepareHandler != null) {
            prepareHandler.execute(data);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void stmtClose(byte[] data) {
        if (prepareHandler != null) {
            prepareHandler.close(data);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
        }
    }

    public void heartbeat(byte[] data) {
        write(writeToBuffer(OkPacket.OK, allocate()));
    }

    public void kill(byte[] data) {
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
    }

    public void unknown(byte[] data) {
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
    }

    @Override
    public void register() throws IOException {
        if (!isClosed.get()) {

            // 生成认证数据
            byte[] rand1 = RandomUtil.randomBytes(8);
            byte[] rand2 = RandomUtil.randomBytes(12);

            // 保存认证数据
            byte[] seed = new byte[rand1.length + rand2.length];
            System.arraycopy(rand1, 0, seed, 0, rand1.length);
            System.arraycopy(rand2, 0, seed, rand1.length, rand2.length);
            this.seed = seed;

            // 发送握手数据包
            boolean useHandshakeV10 = MycatServer.getInstance().getConfig().getSystem().getUseHandshakeV10() == 1;
            if (useHandshakeV10) {
                HandshakeV10Packet hs = new HandshakeV10Packet();
                hs.packetId = 0;
                hs.protocolVersion = Versions.PROTOCOL_VERSION;
                hs.serverVersion = Versions.SERVER_VERSION;
                hs.threadId = id;
                hs.seed = rand1;
                hs.serverCapabilities = getServerCapabilities();
                hs.serverCharsetIndex = (byte) (charsetIndex & 0xff);
                hs.serverStatus = 2;
                hs.restOfScrambleBuff = rand2;
                hs.write(this);
            } else {
                HandshakePacket hs = new HandshakePacket();
                hs.packetId = 0;
                hs.protocolVersion = Versions.PROTOCOL_VERSION;
                hs.serverVersion = Versions.SERVER_VERSION;
                hs.threadId = id;
                hs.seed = rand1;
                hs.serverCapabilities = getServerCapabilities();
                hs.serverCharsetIndex = (byte) (charsetIndex & 0xff);
                hs.serverStatus = 2;
                hs.restOfScrambleBuff = rand2;
                hs.write(this);
            }

            // asynread response
            this.asynRead();
        }
    }

    @Override
    public void handle(final byte[] data) {

        if (isSupportCompress()) {
            List<byte[]> packs = CompressUtil.decompressMysqlPacket(data, decompressUnfinishedDataQueue);
            for (byte[] pack : packs) {
                if (pack.length != 0) {
                    rawHandle(pack);
                }
            }
        } else {
            rawHandle(data);
        }
    }

    public void rawHandle(final byte[] data) {
        //load data infile  客户端会发空包 长度为4
        if (data.length == 4 && data[0] == 0 && data[1] == 0 && data[2] == 0) {
            // load in data空包
            loadDataInfileHandler.end(data[3]);
            //loadDataInfileEnd(data[3]);
            return;
        }
        //修改quit的判断,当load data infile 分隔符为\001 时可能会出现误判断的bug.
        if (data.length > 4 && data[0] == 1 && data[1] == 0 && data[2] == 0 && data[3] == 0
            && data[4] == MySQLPacket.COM_QUIT) {
            this.getProcessor().getCommands().doQuit();
            this.close("quit cmd");
            return;
        }
        handler.handle(data);
    }

    protected int getServerCapabilities() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        boolean usingCompress = MycatServer.getInstance().getConfig().getSystem().getUseCompression() == 1;
        if (usingCompress) {
            flag |= Capabilities.CLIENT_COMPRESS;
        }

        flag |= Capabilities.CLIENT_ODBC;
        flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= ServerDefs.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        flag |= Capabilities.CLIENT_MULTI_RESULTS;
        boolean useHandshakeV10 = MycatServer.getInstance().getConfig().getSystem().getUseHandshakeV10() == 1;
        if (useHandshakeV10) {
            flag |= Capabilities.CLIENT_PLUGIN_AUTH;
        }
        return flag;
    }

    protected boolean isConnectionReset(Throwable t) {
        if (t instanceof IOException) {
            String msg = t.getMessage();
            return (msg != null && msg.contains("Connection reset by peer"));
        }
        return false;
    }

    private final static byte[] encodeString(String src, String charset) {
        if (src == null) {
            return null;
        }
        if (charset == null) {
            return src.getBytes();
        }
        try {
            return src.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return src.getBytes();
        }
    }

    /**
     * 设置是否需要中断当前事务
     */
    public void setTxInterrupt(String txInterrputMsg) {
        if (!autocommit && !txInterrupted) {
            txInterrupted = true;
            this.txInterrputMsg = txInterrputMsg;
        }
    }

    public boolean isTxInterrupted() {
        return txInterrupted;
    }

    @Override
    public CommandCount getCommands() {
        return processor.getCommands();
    }

    /* @Override
    public void close(String reason) {
        super.close(isAuthenticated ? reason : "");
    }*/

    public boolean isAutocommit() {
        return autocommit;
    }

    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    public long getLastInsertId() {
        return lastInsertId;
    }

    public void setLastInsertId(long lastInsertId) {
        this.lastInsertId = lastInsertId;
    }

    public Session getSession2() {
        return session;
    }

    public void setSession2(NonBlockingSession session2) {
        this.system = session2.getCurrentConfig().getSystem();
        this.session = session2;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    @Override
    public void ping() {
        Ping.response(this);
    }

    public void execute(String sql, int type) {
        //连接状态检查
        if (this.isClosed()) {
            LOGGER.warn("ignore execute ,server connection is closed {}", this);
            return;
        }
        // 事务状态检查
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback." + txInterrputMsg);
            return;
        }

        MycatConfig config = session.getCurrentConfig();

        // 检查当前使用的DB
        String db = this.schema;
        boolean isDefault = true;
        if (db == null) {
            db = SchemaUtil.detectDefaultDb(sql, type, session);
            if (db == null) {
                writeErrMessage(ErrorCode.ERR_BAD_LOGICDB, "No MyCAT Database selected");
                return;
            }
            isDefault = false;
        }

        if (SQLHandler.Type.SELECT == type) {
            if (db.equalsIgnoreCase("information_schema")) {
                MysqlInformationSchemaHandler.handle(sql, this);
                return;
            }
            if ("mysql".equalsIgnoreCase(db)) {
                MysqlProcHandler.handle(sql, this);
                return;
            }
        }

        //检查所选的DB是否具有mycat对应的配置
        SchemaConfig schemaCfg = config.getSchemas().get(db);
        if (schemaCfg == null) {
            writeErrMessage(ErrorCode.ERR_BAD_LOGICDB, "Unknown MyCAT Database '" + db + "'");
            return;
        }

        /*
                           当已经设置默认schema时，可以通过在sql中指定其它schema的方式执行
         * 相关sql，已经在mysql客户端中验证。
         * 所以在此处增加关于sql中指定Schema方式的支持。
         */
        if (isDefault && schemaCfg.isCheckSQLSchema() && isNormalSql(type)) {
            String dbInSql = session.getSqlCtx().getVistor().getSchema();
            if (dbInSql != null && !db.equals(dbInSql)) {
                schemaCfg = config.getSchemas().getOrDefault(dbInSql, schemaCfg);
            }
        }

        routeEndExecuteSQL(sql, type, schemaCfg);

    }

    protected boolean isNormalSql(int type) {
        return SQLHandler.Type.SELECT == type || SQLHandler.Type.INSERT == type || SQLHandler.Type.UPDATE == type
               || SQLHandler.Type.DELETE == type || SQLHandler.Type.DDL == type;
    }

    public RouteResultset routeSQL(String sql, int type) {

        String db = this.schema;
        if (db == null) {
            writeErrMessage(ErrorCode.ERR_BAD_LOGICDB, "No MyCAT Database selected");
            return null;
        }

        SchemaConfig schema = session.getCurrentConfig().getSchemas().get(db);
        if (schema == null) {
            writeErrMessage(ErrorCode.ERR_BAD_LOGICDB, "Unknown MyCAT Database '" + db + "'");
            return null;
        }

        try {
            return router.route(system, schema, type, sql, charset, this);
        } catch (Exception e) {
            LOGGER.warn(sql, e);
            String msg = e.getMessage();
            writeErrMessage(ErrorCode.ER_PARSE_ERROR, msg == null ? e.toString() : msg);
            return null;
        }
    }

    // 路由计算
    public void routeEndExecuteSQL(String sql, int type, SchemaConfig schema) {
        try {
            RouteResultset rrs = router.route(system, schema, type, sql, charset, this);
            session.execute(rrs, rrs.isSelectForUpdate() ? SQLHandler.Type.UPDATE : type);
        } catch (Exception e) {
            LOGGER.warn(sql, e);
            writeErrMessage(ErrorCode.ER_PARSE_ERROR, e.getMessage() == null ? e.toString() : e.getMessage());
            return;
        }

        LOGGER.debug("can't found route for:{}", sql);
        //insert 语句会另外一个线程处理后返回null,此处不能writeErrMessage(xx)
        // writeErrMessage(ErrorCode.ER_PARSE_ERROR, "Route return empty");

    }

    /**
     * 提交事务
     */
    public void commit() {
        if (txInterrupted) {
            writeErrMessage(ErrorCode.ER_YES, "Transaction error, need to rollback.");
        } else {
            session.commit();
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        // 状态检查
        if (txInterrupted) {
            txInterrupted = false;
        }
        // 执行回滚
        session.rollback();
    }

    /**
     * 执行lock tables语句方法
     * @param sql
     */
    public void lockTable(String sql) {
        // 事务中不允许执行lock table语句
        if (!autocommit) {
            writeErrMessage(ErrorCode.ER_YES, "can't lock table in transaction!");
            return;
        }
        // 已经执行了lock table且未执行unlock table之前的连接不能再次执行lock table命令
        if (isLocked) {
            writeErrMessage(ErrorCode.ER_YES, "can't lock multi-table");
            return;
        }
        RouteResultset rrs = routeSQL(sql, SQLHandler.Type.LOCK);
        if (rrs != null) {
            session.lockTable(rrs);
        }
    }

    /**
     * 执行unlock tables语句方法
     * @param sql
     */
    public void unLockTable(String sql) {
        sql = sql.replaceAll("\n", " ").replaceAll("\t", " ");
        String[] words = SplitUtil.split(sql, ' ', true);
        if (words.length == 2 && ("table".equalsIgnoreCase(words[1]) || "tables".equalsIgnoreCase(words[1]))) {
            isLocked = false;
            session.unLockTable(sql);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
        }

    }

    /**
     * 撤销执行中的语句
     * 
     * @param sponsor
     *            发起者为null表示是自己
     */
    public void cancel(final FrontendConnection sponsor) {
        processor.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                session.cancel(sponsor);
            }
        });
    }

    @Override
    public boolean isIdleTimeout() {
        if (isAuthenticated) {
            return super.isIdleTimeout();
        } else {
            return TimeUtil.currentTimeMillis() > Math.max(lastWriteTime, lastReadTime) + AUTH_TIMEOUT;
        }
    }

    @Override
    public void close(String reason) {
        super.close(reason);
        session.terminate();
        if (getLoadDataInfileHandler() != null) {
            getLoadDataInfileHandler().clear();
        }
    }

    /**
     * add huangyiming 检测字符串中某字符串出现次数
     * @param srcText
     * @param findText
     * @return
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    public boolean isPreAcStates() {
        return preAcStates;
    }

    public void setPreAcStates(boolean preAcStates) {
        this.preAcStates = preAcStates;
    }

    @Override
    public String getName() {
        return getProcessor().getName();
    }

    @Override
    public BufferPool getBufferPool() {
        return processor.getBufferPool();
    }

    /** 
     * @see io.mycat.net.plus.ClientConn#flushWriteBuffer()
     */
    @Override
    public void flushWriteBuffer() {
        getSocketWR().doNextWriteCheck();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[thread=").append(Thread.currentThread().getName()).append(",class=")
            .append(getClass().getSimpleName()).append(",id=").append(id).append(",host=").append(host).append(",port=")
            .append(port).append(",schema=").append(schema).append(']').toString();
    }
}