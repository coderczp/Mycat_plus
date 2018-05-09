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

package io.mycat.server.handler.plus.impl;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlKillStatement;

import io.mycat.MycatServer;
import io.mycat.config.ErrorCode;
import io.mycat.net.NIOProcessor;
import io.mycat.net.mysql.OkPacket;
import io.mycat.net.plus.ClientConn;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.parser.plus.StatementHolder;
import io.mycat.util.StringUtil;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月26日 上午11:49:03 
 */
public class KillHandler implements SQLHandler {

    @Override
    public int type() {
        return SQLHandler.Type.KILL;
    }

    @Override
    public void handle(String stmt, ClientConn c) {
        StatementHolder stm = c.getSession2().getSqlCtx().getStmtHolder();
        MySqlKillStatement kill = (MySqlKillStatement) stm.getStmt();
        String id =  kill.getThreadId().toString();
       
        if (StringUtil.isEmpty(id)) {
            c.writeErrMessage((byte) 1, ErrorCode.ER_NO_SUCH_THREAD, "NULL connection id");
        } else {
            // get value
            long value = 0;
            try {
                value = Long.parseLong(id);
            } catch (NumberFormatException e) {
                c.writeErrMessage((byte) 1, ErrorCode.ER_NO_SUCH_THREAD, "Invalid connection id:" + id);
                return;
            }

            // kill myself
            if (value == c.getId()) {
                getOkPacket().write(c);
                c.write(c.allocate());
                return;
            }

            // get connection and close it
            ClientConn fc = null;
            NIOProcessor[] processors = MycatServer.getInstance().getProcessors();
            for (NIOProcessor p : processors) {
                if ((fc = p.getFrontends().get(value)) != null) {
                    break;
                }
            }
            if (fc != null) {
                fc.close("killed");
                getOkPacket().write(c);
            } else {
                c.writeErrMessage((byte) 1, ErrorCode.ER_NO_SUCH_THREAD, "Unknown connection id:" + id);
            }
        }
    }

    private static OkPacket getOkPacket() {
        OkPacket packet = new OkPacket();
        packet.packetId = 1;
        packet.affectedRows = 0;
        packet.serverStatus = 2;
        return packet;
    }

}
