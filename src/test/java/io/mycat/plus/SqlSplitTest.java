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

import org.junit.Assert;
import org.junit.Test;

import io.mycat.server.parser.SimpleSqlParser;

/**
 * @TODO 类说明
 * @author jeff.cao
 * @version 0.0.1, 2018年4月25日 下午11:37:56 
 */
public class SqlSplitTest {

    @Test
    public static void testSplit() {
        String sql="use db;select * from tbl where name='abc;xdd;   ddd;';update tbl set name=';sdd';";
        List<String> sqls = SimpleSqlParser.safeSplit(sql);
        System.out.println(sqls);
        Assert.assertTrue("use db".equals(sqls.get(0)));
        Assert.assertTrue("update tbl set name=';sdd'".equals(sqls.get(2)));
        Assert.assertTrue("select * from tbl where name='abc;xdd;   ddd;'".equals(sqls.get(1)));
    }
    
    public static void main(String[] args) {
        testSplit();
    }
}
