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
package io.mycat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.net.handler.FrontendQueryHandler;
import io.mycat.net.plus.ClientConn;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.handler.plus.SQLHandlerManager;
import io.mycat.server.parser.SimpleSqlParser;

/***
 * 
 * 分发sql
 * @author jeff.cao
 * @version 0.0.1, 2018年4月25日 下午4:55:04
 */
public class HandlerDispatch implements FrontendQueryHandler {

    protected ClientConn        conn;
    protected Boolean           readOnly;
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerDispatch.class);

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public HandlerDispatch(ClientConn source) {
        this.conn = source;
    }

    @Override
    public void query(String stmt) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} {}", conn, stmt);
        }

        //int rs = SimpleSqlParser.parse(stmt);
        //int sqlType = rs & 0xff;
        //int offset = rs >>> 8;
        //SQLHandler handler = SQLHandlerManager.get(sqlType);
        int sqlType = conn.getSession2().getSqlCtx().getType();
        SQLHandler handler = SQLHandlerManager.get(sqlType);
        if (handler != null) {
            handler.handle(stmt, conn);
            return;
        }

        if (readOnly) {
            SQLHandlerManager.READ_ONLY.handle(stmt, conn);
            return;
        }

        LOGGER.debug("parse:{} return type:{}", stmt, sqlType);
        conn.execute(stmt, sqlType);
    }

    /*protected void dispatch(String sql, ClientConn c) {
        int rs = ServerParse.parse(sql);
        int sqlType = rs & 0xff;
        int offset = rs >>> 8;
    
        switch (sqlType) {
            //explain sql
            case SQLHandler.Type.EXPLAIN:
                ExplainHandler.handle(sql, c, offset);
                break;
            case SQLHandler.Type.EXPLAIN2:
                //explain2 datanode=? sql=?
                Explain2Handler.handle(sql, c, offset);
                break;
            case SQLHandler.Type.SET:
                SetHandler.handle(sql, c, offset);
                break;
            case SQLHandler.Type.SHOW:
                ShowHandler.handle(sql, c, offset);
                break;
            case SQLHandler.Type.SELECT:
                SelectHandler.handle(sql, c, offset);
                break;
            case SQLHandler.Type.START:
                StartHandler.handle(sql, c, offset);
                break;
            case SQLHandler.Type.BEGIN:
                BeginHandler.handle(sql, c);
                break;
            //不支持oracle的savepoint事务回退点
            case SQLHandler.Type.SAVEPOINT:
                SavepointHandler.handle(sql, c);
                break;
            case SQLHandler.Type.KILL:
                KillHandler.handle(sql, offset, c);
                break;
            case SQLHandler.Type.HELP:
            case SQLHandler.Type.KILL_QUERY:
                LOGGER.warn("Unsupported command:{}", sql);
                c.writeErrMessage((byte) 1, ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case SQLHandler.Type.USE:
                UseHandler.handle(sql, c, offset);
                break;
            case SQLHandler.Type.COMMIT:
                c.commit();
                break;
            case SQLHandler.Type.ROLLBACK:
                c.rollback();
                break;
            case SQLHandler.Type.MYSQL_CMD_COMMENT:
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            case SQLHandler.Type.MYSQL_COMMENT:
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            case SQLHandler.Type.LOAD_DATA_INFILE_SQL:
                //c.loadDataInfileStart(sql);
                c.getLoadDataInfileHandler().start(sql);
                break;
            case SQLHandler.Type.LOCK:
                c.lockTable(sql);
                break;
            case SQLHandler.Type.UNLOCK:
                c.unLockTable(sql);
                break;
            default:
                if (readOnly) {
                    LOGGER.warn("User readonly:{}", sql);
                    c.writeErrMessage((byte) 1, ErrorCode.ER_USER_READ_ONLY, "User readonly");
                    break;
                }
                c.execute(sql, sqlType);
        }
    }*/

}
