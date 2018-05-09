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

package io.mycat.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.web.bean.LogicHost;
import io.mycat.web.bean.LogicTable;
import io.mycat.web.bean.PhysicsHost;
import io.mycat.web.bean.ShardingNode;
import io.mycat.web.controller.LogicTableController.ShardingNodeInfo;

/**
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月8日 下午2:30:01 
 */
public class MySqlUtils {

    private static Logger LOG = LoggerFactory.getLogger(MySqlUtils.class);

    /**
     * 
     * @param arg
     * @param host
     * @throws SQLException 
     */
    public static void createDB(ShardingNode arg, LogicHost host) throws SQLException {
        for (PhysicsHost item : host.getPhysicsdbs()) {
            Connection conn = getConn(item, null);
            try {
                String createDB = String.format("create database `%s`", arg.getPhysicsdb());
                Statement st = conn.createStatement();
                int res = st.executeUpdate(createDB);
                st.close();
                if (res > 0) {
                    LOG.info("success to create db:{} in{}", arg.getName(), item);
                } else {
                    LOG.info("fail to create db:{} in{}", arg.getName(), item);
                }
            } finally {
                conn.close();
            }
        }
    }

    /**
     * 字段创建分片表
     * @param arg
     * @param infos
     * @throws SQLException 
     */
    public static void createTable(LogicTable arg, List<ShardingNodeInfo> infos) throws SQLException {
        for (ShardingNodeInfo info : infos) {
            for (PhysicsHost host : info.physicsHosts) {
                Connection conn = getConn(host, info.node.getPhysicsdb());
                try {
                    Statement st = conn.createStatement();
                    st.executeUpdate(arg.getSql());
                    st.close();
                    LOG.info("success to create table:{} in:{}", arg.getName(), host);
                } finally {
                    conn.close();
                }
            }
        }
    }

    /**
     * 创建连接
     */
    private static Connection getConn(PhysicsHost item, String db) throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s?characterEncoding=utf8", item.getHost(), item.getPort());
        if (db != null && db.length() > 0) {
            url = String.format("jdbc:mysql://%s:%s/%s?characterEncoding=utf8", item.getHost(), item.getPort(), db);
        }
        return DriverManager.getConnection(url, item.getUser(), item.getPassword());
    }

}
