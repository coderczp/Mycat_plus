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

package io.mycat.plus;

import java.util.List;

import org.junit.Test;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlKillStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

import io.mycat.server.parser.SimpleSqlParser;
import junit.framework.Assert;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月25日 下午5:59:49 
 */
public class DruidTest {

    @Test
    public void testSelectFunction() {
        String sql = "select SYSDATE();";
        MySqlStatementParser parser = new MySqlStatementParser(sql, true);
        SQLSelectStatement parseSelect = (SQLSelectStatement) parser.parseSelect();
        SQLSelect select = parseSelect.getSelect();
        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) select.getQuery();
        SQLSelectItem sqlSelectItem = query.getSelectList().get(0);
        SQLExpr expr = sqlSelectItem.getExpr();
        SQLMethodInvokeExpr method = (SQLMethodInvokeExpr) expr;
        String methodName = method.getMethodName();
        Assert.assertEquals(sql + " SQL函数解析错误", methodName, "SYSDATE");
    }

    @Test
    public void testSimpleParse() {
        String sql = "kill 1234;";
        for (int i = 0; i < 1000000; i++) {
            SimpleSqlParser.parse(sql);
        }
    }

    @Test
    public void testLexer() {
        String sql = "select sysdate();USE testdb;update tpl set name='xx';delete from tpl where id=xx;";
        MySqlStatementParser parser = new MySqlStatementParser(sql, true);
        List<SQLStatement> list = parser.parseStatementList();
        for (SQLStatement stmt : list) {
            if (stmt instanceof SQLSelectStatement) {
                System.out.println("select");
            } else if (stmt instanceof SQLUseStatement) {
                System.out.println("use");
            }
        }
    }
}
