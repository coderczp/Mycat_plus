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

package io.mycat.server.handler.plus;

import io.mycat.net.plus.ClientConn;

/**
 * SQL处理接口
 * @author jeff.cao
 * @version 0.0.1, 2018年4月26日 上午11:12:10 
 */
public interface SQLHandler {

    /**
     * 处理SQL
     * @param stmt :原始的SQL
     * @param c
     * @param offset:
     */
    void handle(String stmt, ClientConn c);

    /***
     * 处理的类型,不支持处理多个
     * 
     * @return
     */
    int type();

    /***
     * 目前支持的所有类型,可以考虑优化为递增,那样可以用数组快速定位
     * @author jeff.cao
     * @version 0.0.1, 2018年4月26日 上午11:15:47
     */
    interface Type {
        int ALL                  = -999;
        int OTHER                = -1;
        int BEGIN                = 1;
        int COMMIT               = 2;
        int DELETE               = 3;
        int INSERT               = 4;
        int REPLACE              = 5;
        int ROLLBACK             = 6;
        int SELECT               = 7;
        int SET                  = 8;
        int SHOW                 = 9;
        int START                = 10;
        int UPDATE               = 11;
        int KILL                 = 12;
        int SAVEPOINT            = 13;
        int USE                  = 14;
        int EXPLAIN              = 15;
        int KILL_QUERY           = 16;
        int HELP                 = 17;
        int MYSQL_CMD_COMMENT    = 18;
        int MYSQL_COMMENT        = 19;
        int CALL                 = 20;
        int DESCRIBE             = 21;
        int LOCK                 = 22;
        int UNLOCK               = 23;
        int CREATE               = 24;
        int ALTER                = 25;
        int DROP_TABLE           = 26;
        int DROP_USER            = 27;
        int DROP_INDEX           = 28;
        int DROP_VIEW            = 29;
        int DROP_TRIGGER         = 30;
        int DROP_DB              = 31;
        int DROP_FUNCTION        = 32;
        int DROP_TABLESPACE      = 33;
        int DROP_PROCEDURE       = 34;
        int CACHE                = 35;
        int DROP_SEQUENCE        = 36;
        int TRUNCATE             = 37;
        int GRANT                = 38;
        int REVOKE               = 39;
        int LBRACE               = 40;
        int RENAME               = 41;
        int RELEASE              = 42;
        int MERGE                = 43;
        int UPSERT               = 44;
        int SUB_SELECT           = 45;
        int PREPARE              = 46;
        int EXECUTE              = 47;
        int DEALLOCATE           = 48;
        int BINLOG               = 49;
        int RESET                = 50;
        int ANALYZE              = 51;
        int OPTIMIZE             = 52;
        int SELECT_WITH_HINT     = 53;
        int HINT                 = 54;

        int LOAD_DATA_INFILE_SQL = 99;
        int EXPLAIN2             = 151;
        int DDL                  = 100;
        int MIGRATE              = 203;
    }
}
