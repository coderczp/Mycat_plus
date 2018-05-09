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
package io.mycat.server.handler.plus.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import com.alibaba.druid.sql.ast.statement.SQLUseStatement;

import io.mycat.config.ErrorCode;
import io.mycat.net.handler.FrontendPrivileges;
import io.mycat.net.mysql.OkPacket;
import io.mycat.net.plus.ClientConn;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.parser.plus.StatementHolder;
import io.mycat.util.StringUtil;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月26日 下午1:03:21
 */
public final class UseHandler implements SQLHandler {

    @Override
    public void handle(String sql, ClientConn c) {
        
        StatementHolder stmt = c.getSession2().getSqlCtx().getStmtHolder();
        SQLUseStatement use = (SQLUseStatement) stmt.getStmt();
        //String schema = sql.substring(offset).trim();
        String schema =  use.getDatabase().getSimpleName();
        int length = schema.length();
        if (length > 0) {
            int end = schema.indexOf(";");
            if (end > 0) {
                schema = schema.substring(0, end);
            }
            schema = StringUtil.replaceChars(schema, "`", null);
            length = schema.length();
            if (schema.charAt(0) == '\'' && schema.charAt(length - 1) == '\'') {
                schema = schema.substring(1, length - 1);
            }
        }
        // 检查schema的有效性
        FrontendPrivileges privileges = c.getPrivileges();
        if (schema == null || !privileges.schemaExists(schema)) {
            c.writeErrMessage((byte) 1, ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + schema + "'");
            return;
        }
        String user = c.getUser();
        if (!privileges.userExists(user, c.getHost())) {
            c.writeErrMessage((byte) 1, ErrorCode.ER_ACCESS_DENIED_ERROR,
                "Access denied for user '" + c.getUser() + "'");
            return;
        }
        Set<String> schemas = privileges.getUserSchemas(user);
        if (schemas == null || schemas.size() == 0 || schemas.contains(schema)) {
            c.setSchema(schema);
            ByteBuffer buffer = c.allocate();
            c.write(c.writeToBuffer(OkPacket.OK, buffer));
        } else {
            String msg = "Access denied for user '" + c.getUser() + "' to database '" + schema + "'";
            c.writeErrMessage((byte) 1, ErrorCode.ER_DBACCESS_DENIED_ERROR, msg);
        }
    }

    @Override
    public int type() {
        return SQLHandler.Type.USE;
    }

}