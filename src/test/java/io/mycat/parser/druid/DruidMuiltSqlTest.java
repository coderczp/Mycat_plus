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

package io.mycat.parser.druid;

import java.util.List;

import org.junit.Test;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

/**
 * @TODO 类说明
 * @author jeff.cao
 * @version 0.0.1, 2018年4月22日 下午6:23:42 
 */
public class DruidMuiltSqlTest {

    public static void main(String[] args) {
        parseBatchSql();
    }

    @Test
    public static void parseBatchSql() {
        String sql0 = "INSERT INTO db1.tbl1 (id,`Name`) VALUES(1,'name');";
        String sql1 = "INSERT INTO db2.tbl2 (id,`name`) VALUES(1,'name');";
        String sql = sql0 + sql1;

        int size = 1;
        long st = System.currentTimeMillis();
        //MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        for (int i = 0; i < size; i++) {
            MySqlStatementParser parser = new MySqlStatementParser(sql);
            List<SQLStatement> stmtList = parser.parseStatementList();
            for (SQLStatement stmt : stmtList) {
                if(stmt instanceof MySqlInsertStatement) {
                    MySqlInsertStatement insert = (MySqlInsertStatement) stmt;
                    System.out.println(insert.getColumns());
                    System.out.println(insert.getValuesList());
                }
//                stmt.accept(visitor);
//                String tbl = visitor.getCurrentTable();
//                Column column = visitor.getColumn(tbl, "name");
//                System.out.println(column);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - st);
    }
}
