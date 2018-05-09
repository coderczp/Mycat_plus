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

package io.mycat.net.plus;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.mycat.buffer.BufferPool;
import io.mycat.net.NIOConnection;
import io.mycat.net.NIOHandler;
import io.mycat.net.NIOProcessor;
import io.mycat.net.handler.FrontendPrivileges;
import io.mycat.net.handler.LoadDataInfileProcessor;
import io.mycat.route.RouteResultset;
import io.mycat.server.Session;
import io.mycat.statistic.CommandCount;

/**
 * 来自客户端的连接 
 * @author jeff.cao
 * @version 0.0.1, 2018年4月25日 上午11:37:49 
 */
public interface ClientConn extends NIOConnection {

    /**
     * 获取权限
     * @return
     */
    FrontendPrivileges getPrivileges();

    /**
     * 登录seed
     * @return
     */
    byte[] getSeed();

    /**
     * 是否登录成功
     * @param b
     */
    void setAuthenticated(boolean b);

    /**
     * 
     * @param b
     * @param errno
     * @param info
     */
    void writeErrMessage(byte b, int errno, String info);

    /**
     * 
     * @return
     */
    ByteBuffer allocate();

    /**
     * 
     * @param b
     */
    void setSupportCompress(boolean b);

    /**
     * 
     * @param authOk
     * @param buffer
     * @return
     */
    ByteBuffer writeToBuffer(byte[] authOk, ByteBuffer buffer);

    /**
     * 
     * @param user
     */
    void setUser(String user);

    /**
     * 
     * @param database
     */
    void setSchema(String database);

    /**
     * 
     * @param charsetIndex
     */
    boolean setCharsetIndex(int charsetIndex);

    /**
     * 
     * @param frontendCommandHandler
     */
    void setHandler(NIOHandler handler);

    /**
     * 
     * @param data
     */
    void initDB(byte[] data);

    /**
     * 
     * @param data
     */
    void query(byte[] data);

    /**
     * 
     */
    void ping();

    /**
     * 
     * @param data
     */
    void kill(byte[] data);

    /**
     * 
     * @param data
     */
    void stmtPrepare(byte[] data);

    /**
     * 
     * @param data
     */
    void stmtSendLongData(byte[] data);

    /**
     * 
     * @param data
     */
    void stmtReset(byte[] data);

    /**
     * 
     * @param data
     */
    void stmtExecute(byte[] data);

    /**
     * 
     * @param data
     */
    void stmtClose(byte[] data);

    /**
     * 
     * @param data
     */
    void heartbeat(byte[] data);

    /**
     * 
     * @return
     */
    CommandCount getCommands();

    /**
     * 
     * @return
     */
    int getTxIsolation();

    /**
     * 
     * @return
     */
    int getPacketHeaderSize();

    /**
     * 
     * @param buffer
     * @param i
     * @param writeSocketIfFull
     * @return
     */
    ByteBuffer checkWriteBuffer(ByteBuffer buffer, int i, boolean writeSocketIfFull);

    /**
     * 
     * @return
     */
    Session getSession2();

    /**
     * 
     * @return
     */
    int getCharsetIndex();

    /**
     * 
     * @return
     */
    boolean isAutocommit();

    /**
     * 
     * @return
     */
    boolean isPreAcStates();

    /**
     * 
     * @param b
     */
    void setAutocommit(boolean b);

    /**
     * 
     * @param errInfo
     */
    void setTxInterrupt(String errInfo);

    /**
     * 
     * @param ok
     */
    void write(byte[] ok);

    /**
     * 
     * @return
     */
    boolean isLocked();

    /**
     * 
     * @param object
     */
    void setExecuteSql(String sql);

    /**
     * 
     * @return
     */
    String getSchema();

    /**
     * 
     * @param stmt
     * @param show
     */
    void execute(String stmt, int type);

    /**
     * 
     * @return
     */
    String getUser();

    /**
     * 
     * @param buffer
     */
    void recycle(ByteBuffer buffer);

    /**
     * 
     * @param insertId
     */
    void setLastInsertId(long insertId);

    /**
     * 
     * @return
     */
    long getId();

    /**
     * 
     * @param b
     */
    void setLocked(boolean b);

    /**
     * 
     * @param b
     */
    void setPreAcStates(boolean b);

    /**
     * 
     */
    void commit();

    /**
     * 
     * @param readCommitted
     */
    void setTxIsolation(int readCommitted);

    /**
     * 
     * @param charset
     * @return
     */
    boolean setCharset(String charset);

    /**
     * 
     * @return
     */
    boolean isTxInterrupted();

    /**
     * 
     * @param currentTimeMillis
     */
    void setLastWriteTime(long currentTimeMillis);

    /**
     * 
     * @return
     */
    String getName();

    /**
     * 
     * @return
     */
    ByteBuffer getReadBuffer();

    /**
     * 
     */
    void rollback();

    /**
     * 
     * @param sql
     */
    void lockTable(String sql);

    /**
     * 
     * @param sql
     */
    void unLockTable(String sql);

    /**
     * 
     * @param data
     */
    //void loadDataInfileData(byte[] data);

    /**
     * @param sql
     */
    // void loadDataInfileStart(String sql);
    /**
     * 
     * @return
     * */
    LoadDataInfileProcessor getLoadDataInfileHandler();

    /**
     * 
     * @param sql
     */
    void query(String sql);

    /**
     * 
     * @return
     */
    long getLastInsertId();

    /**
     * 
     * @param insertSql
     * @param insert
     * @return
     */
    RouteResultset routeSQL(String insertSql, int insert);

    ConcurrentLinkedQueue<ByteBuffer> getWriteQueue();

    /**
     * 
     * @return
     */
    BufferPool getBufferPool();

    /**
     * 
     */
    void flushWriteBuffer();

    /**
     * 
     * @return
     */
    long getLastReadTime();

    /**
     * 
     * @return
     */
    long getLastWriteTime();

    /**
     * 
     * @return
     */
    String getExecuteSql();

    /**
     * 
     * @param id
     */
    void setId(long id);

    /**
     * 
     * @param packetHeaderSize
     */
    void setPacketHeaderSize(int packetHeaderSize);

    /**
     * 
     * @param maxPacketSize
     */
    void setMaxPacketSize(int maxPacketSize);

    /**
     * 
     * @param idleTimeout
     */
    void setIdleTimeout(long idleTimeout);

    /**
     * 
     * @param processor
     */
    void setProcessor(NIOProcessor processor);

}
