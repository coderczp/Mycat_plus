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

package io.mycat.server;

import com.alibaba.druid.sql.ast.SQLStatement;

import io.mycat.route.parser.druid.MycatSchemaStatVisitor;
import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.parser.plus.StatementHolder;

/**
 * SQL上下文,包含SQL AST,在入口处统一生成,避免后续重复创建,生命周期为当前会话
 * 
 * @author jeff.cao
 * @version 0.0.1, 2018年4月26日 下午5:15:26 
 */
public class SqlContext {

    public static class DBTable {
        final String db;
        final String table;

        public DBTable(String db, String table) {
            this.db = db;
            this.table = table;
        }

        public String getDb() {
            return db;
        }

        public String getTable() {
            return table;
        }

    }

    /**{@link SQLHandler.Type} SQL的类型*/
    private int                    type;
    private String                 sql;
    private StatementHolder        stmt;
    private MycatSchemaStatVisitor vistor = new MycatSchemaStatVisitor();

    public void setStmt(StatementHolder holder) {
        SQLStatement stmt = holder.getStmt();
        stmt.accept(vistor);
        this.stmt = holder;
        this.sql = stmt.toString();
        this.type = holder.getType();
    }

    public MycatSchemaStatVisitor getVistor() {
        return vistor;
    }

    public StatementHolder getStmtHolder() {
        return stmt;
    }

    public String getSql() {
        return sql;
    }

    public int getType() {
        return type;
    }

}
