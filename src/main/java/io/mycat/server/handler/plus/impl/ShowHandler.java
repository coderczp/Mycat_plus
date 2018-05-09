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

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLShowTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowDatabasesStatement;

import io.mycat.net.plus.ClientConn;
import io.mycat.server.SqlContext;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.parser.plus.StatementHolder;
import io.mycat.server.response.ShowDatabases;
import io.mycat.server.response.ShowFullTables;

/***
 * @author jeff.cao
 * @version 0.0.1, 2018年4月26日 下午1:05:12
 */
public class ShowHandler implements SQLHandler {

    @Override
    public void handle(String stmt, ClientConn c) {

        SqlContext ctx = c.getSession2().getSqlCtx();
        StatementHolder holder = ctx.getStmtHolder();
        SQLStatement show = holder.getStmt();

        if (show instanceof MySqlShowDatabasesStatement) {
            ShowDatabases.response(c);
        } else if (show instanceof SQLShowTablesStatement) {
            ShowFullTables.response(c, stmt);
        } else {
            c.execute(stmt, SQLHandler.Type.SHOW);
        }

        /*
        // 排除 “ ` ” 符号
        stmt = StringUtil.replaceChars(stmt, "`", null);
        int type = ServerParseShow.parse(stmt, offset);
        switch (type) {
            case ServerParseShow.DATABASES:
                ShowDatabases.response(c);
                break;
            case ServerParseShow.TABLES:
                ShowTables.response(c, stmt, type);
                break;
            case ServerParseShow.FULLTABLES:
                ShowFullTables.response(c, stmt, type);
                break;
            case ServerParseShow.MYCAT_STATUS:
                ShowMyCatStatus.response(c);
                break;
            case ServerParseShow.MYCAT_CLUSTER:
                ShowMyCATCluster.response(c);
                break;
            default:
                c.execute(stmt, SQLHandler.Type.SHOW);
        }
        */}

    @Override
    public int type() {
        return SQLHandler.Type.SHOW;
    }

}