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

import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;

import io.mycat.config.ErrorCode;
import io.mycat.net.plus.ClientConn;
import io.mycat.server.SqlContext;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.response.SelectDatabase;
import io.mycat.server.response.SelectLastInsertId;
import io.mycat.server.response.SelectTxReadOnly;
import io.mycat.server.response.SelectUser;
import io.mycat.server.response.SelectVariables;
import io.mycat.server.response.SelectVersion;
import io.mycat.server.response.SelectVersionComment;
import io.mycat.server.response.SessionIncrement;
import io.mycat.server.response.SessionIsolation;

/***
 * 
 * @author jeff.cao
 * @version 0.0.1, 2018年4月26日 下午1:42:29
 */
public final class SelectHandler implements SQLHandler {

    @Override
    public int type() {
        return SQLHandler.Type.SELECT;
    }

    public void handle(String stmt, ClientConn c) {

        SqlContext ctx = c.getSession2().getSqlCtx();
        if (ctx.getVistor().getCurrentTable() != null) {
            //TODO 要修改来支持 select @@var
            c.execute(stmt, SQLHandler.Type.SELECT);
            return;
        }

        SQLSelectStatement selectStmt = (SQLSelectStatement) ctx.getStmtHolder().getStmt();
        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) selectStmt.getSelect().getQuery();

        //select databases();等等
        List<SQLSelectItem> selectList = query.getSelectList();
        for (SQLSelectItem item : selectList) {
            SQLExpr expr = item.getExpr();
            if (!(expr instanceof SQLMethodInvokeExpr)) {
                c.writeErrMessage((byte) 1, ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
                return;
            }

            SQLMethodInvokeExpr method = (SQLMethodInvokeExpr) expr;
            String mName = method.getMethodName().toUpperCase();
            if (mName.equals("DATABASES")) {
                SelectDatabase.response(c);
            } else if (mName.equals("VERSION_COMMENT")) {
                SelectVersionComment.response(c);
            } else if (mName.equals("USER")) {
                SelectUser.response(c);
            } else if (mName.equals("VERSION")) {
                SelectVersion.response(c);
            } else if (mName.equals("SESSION_INCREMENT")) {
                SessionIncrement.response(c);
            } else if (mName.equals("SESSION_ISOLATION")) {
                SessionIsolation.response(c);
            } else if (mName.equals("LAST_INSERT_ID")) {
                SelectLastInsertId.response(c, stmt, stmt.lastIndexOf("LAST_INSERT_ID()") + 9);
            } else if (mName.equals("@@IDENTITY")) {
                //TODO 暂时不支持
                c.writeErrMessage((byte) 1, ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
                //SelectIdentity.response(c, stmt, offset, orgName);
            } else if (mName.equals("SELECT_VAR_ALL")) {
                SelectVariables.execute(c, stmt);
            } else if (mName.equals("SESSION_TX_READ_ONLY")) {
                SelectTxReadOnly.response(c);
            } else {
                c.execute(stmt, SQLHandler.Type.SELECT);
            }

        }

        /*
        int offset = offs;
        switch (ServerParseSelect.parse(stmt, offs)) {
            case ServerParseSelect.VERSION_COMMENT:
                SelectVersionComment.response(c);
                break;
            case ServerParseSelect.DATABASE:
                SelectDatabase.response(c);
                break;
            case ServerParseSelect.USER:
                SelectUser.response(c);
                break;
            case ServerParseSelect.VERSION:
                SelectVersion.response(c);
                break;
            case ServerParseSelect.SESSION_INCREMENT:
                SessionIncrement.response(c);
                break;
            case ServerParseSelect.SESSION_ISOLATION:
                SessionIsolation.response(c);
                break;
            case ServerParseSelect.LAST_INSERT_ID:
                // offset = ParseUtil.move(stmt, 0, "select".length());
                loop: for (int l = stmt.length(); offset < l; ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                            continue;
                        case '/':
                        case '#':
                            offset = ParseUtil.comment(stmt, offset);
                            continue;
                        case 'L':
                        case 'l':
                            break loop;
                    }
                }
                offset = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, offset);
                offset = ServerParseSelect.skipAs(stmt, offset);
                SelectLastInsertId.response(c, stmt, offset);
                break;
            case ServerParseSelect.IDENTITY:
                // offset = ParseUtil.move(stmt, 0, "select".length());
                loop: for (int l = stmt.length(); offset < l; ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                            continue;
                        case '/':
                        case '#':
                            offset = ParseUtil.comment(stmt, offset);
                            continue;
                        case '@':
                            break loop;
                    }
                }
                int indexOfAtAt = offset;
                offset += 2;
                offset = ServerParseSelect.indexAfterIdentity(stmt, offset);
                String orgName = stmt.substring(indexOfAtAt, offset);
                offset = ServerParseSelect.skipAs(stmt, offset);
                SelectIdentity.response(c, stmt, offset, orgName);
                break;
            case ServerParseSelect.SELECT_VAR_ALL:
                SelectVariables.execute(c, stmt);
                break;
            case ServerParseSelect.SESSION_TX_READ_ONLY:
                SelectTxReadOnly.response(c);
                break;
            default:
                c.execute(stmt, SQLHandler.Type.SELECT);
        }
        */}

}
