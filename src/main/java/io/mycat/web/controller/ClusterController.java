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

package io.mycat.web.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mycat.config.model.MycatNodeConfig;
import io.mycat.web.config.DbConfigLoader;
import io.mycat.web.dao.MycatNodeConfigDao;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月13日 下午6:55:12 
 */
@RestController
public class ClusterController extends BaseController {

    @Autowired
    private DbConfigLoader     loader;

    @Autowired
    private MycatNodeConfigDao nodeDao;

    @Autowired
    private SqlSessionFactory  factory;

    public static final String PING_API   = "api/cluster/ping";

    public static final String UPDATE_API = "api/cluster/config/update";

    private static Logger      log        = LoggerFactory.getLogger(ClusterController.class);

    @RequestMapping(value = "/cluster/config/update")
    public Object list(String type) {
        try {
            loader.doReload();
        } catch (Exception e) {
            log.error("update config error", e);
            return error("fail");
        }
        return ok("succes");
    }

    @RequestMapping(value = "/cluster/ping")
    public Object ping() {
        return ok("pong");
    }

    @RequestMapping(value = "/pref/on")
    public Object pref() {
        //        Pref.runing = !Pref.runing;
        boolean res = true;
        return ok(res);
    }

    @RequestMapping(value = "/cluster/config/import")
    public Object importConfig(@RequestParam String sqls) throws Exception {
        if (StringUtils.isEmpty(sqls)) {
            return error("sqls is empty");
        }
        SqlSession session = factory.openSession();
        try {
            HashMap<String, String> param = new HashMap<>();
            param.put("sql", sqls);
            int insert = session.insert("cluster.insert", param);
            return ok(insert > 0 ? "success" : "fail");
        } finally {
            session.close();
        }

    }

    @RequestMapping(value = "/cluster/config/export")
    public void export(HttpServletResponse resp) throws Exception {

        PrintWriter writer = resp.getWriter();
        SqlSession session = factory.openSession();
        try {
            HashMap<String, String> param = new HashMap<>();
            param.put("sql", "show tables");
            List<Map<String, String>> res = session.selectList("cluster.query", param);
            List<String> tableNames = new ArrayList<>(res.size());
            for (Map<String, String> map : res) {
                tableNames.addAll(map.values());
            }

            if (tableNames.isEmpty()) {
                writer.println("table not found");
                writer.flush();
                writer.close();
                return;
            }

            //流式返回结果
            resp.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            List<Map<String, Object>> res2;
            for (String tbl : tableNames) {

                if (tbl.contains("cluster_node_info")) {
                    continue;
                }

                param.put("sql", "select * from " + tbl);
                res2 = session.selectList("cluster.query", param);
                for (Map<String, Object> map : res2) {
                    Set<Entry<String, Object>> entrySet = map.entrySet();
                    StringBuilder cols = new StringBuilder();
                    StringBuilder vals = new StringBuilder();
                    int size = map.size();
                    for (Entry<String, Object> entry : entrySet) {
                        cols.append("`").append(entry.getKey()).append("`");
                        vals.append("`").append(entry.getValue()).append("`");
                        if (--size > 0) {
                            cols.append(",");
                            vals.append(",");
                        }
                    }
                    writer.print(String.format("INSERT INTO `%s` (%s) VALUES(%s);\n", tbl, cols, vals));
                }
            }

            writer.flush();
            writer.close();

        } finally {
            session.close();
        }
    }

    @RequestMapping(value = "/cluster/list")
    public Object list() {
        MycatNodeConfig param = new MycatNodeConfig();
        return ok(nodeDao.list(param));
    }
}
