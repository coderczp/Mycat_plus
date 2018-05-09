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

package io.mycat.web.test;

import org.junit.Test;

import io.mycat.route.parser.util.SQLParserUtils;
import junit.framework.Assert;

/**
 * @TODO 类说明
 * @author jeff.cao
 * @version 0.0.1, 2018年4月20日 上午10:12:09 
 */
public class TestSimpleSqlParse {

    @Test
    public void testGetInsertTableName() {
        String sql = "INSERT INTO table_name (列1, 列2,...) VALUES (值1, 值2,....)";
        String tbl = SQLParserUtils.getInsertTableName(sql);
        Assert.assertEquals(tbl, "TABLE_NAME");
    }
}
