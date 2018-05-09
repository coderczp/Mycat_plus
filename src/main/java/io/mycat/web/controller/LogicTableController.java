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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSONObject;

import io.mycat.util.SplitUtil;
import io.mycat.util.StringUtil;
import io.mycat.web.bean.LogicHost;
import io.mycat.web.bean.LogicTable;
import io.mycat.web.bean.PhysicsHost;
import io.mycat.web.bean.ShardingNode;
import io.mycat.web.common.EventFlag;
import io.mycat.web.dao.LogicTableDao;
import io.mycat.web.dao.ShardingNodeDao;
import io.mycat.web.event.Event.Type;
import io.mycat.web.service.LogicHostService;
import io.mycat.web.util.MySqlUtils;

/**
 * API 
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月5日 下午11:45:39 
 */
@RestController
public class LogicTableController extends BaseController {

    public static class ShardingNodeInfo {
        public ShardingNode      node;
        public List<PhysicsHost> physicsHosts;
    }

    @Autowired
    private LogicTableDao    dbDao;

    @Autowired
    private ShardingNodeDao  sDao;

    @Autowired
    private LogicHostService lServer;

    private static Logger    log = LoggerFactory.getLogger(LogicTableController.class);

    @RequestMapping(value = "/table/list")
    public Object list(LogicTable arg) {
        List<LogicTable> list = dbDao.list(arg);
        return ok(list);
    }

    @RequestMapping("/table/add")
    @EventFlag(type = Type.CONFIG_RELOAD)
    public Object add(LogicTable arg) {
        try {
            if (StringUtils.isEmpty(arg.getSql())) {
                return error(message.get("LogicTableController.add.sql.empty"));
            }

            if (StringUtils.isEmpty(arg.getLogicDb())) {
                return error(message.get("LogicTableController.add.logicdb.empty"));
            }

            String shardingNodes = arg.getShardingNodes();
            if (StringUtils.isEmpty(shardingNodes)) {
                return error(message.get("LogicTableController.add.shard.empty"));
            }

            if (StringUtils.isEmpty(arg.getRuleObjJson())) {
                return error(message.get("LogicTableController.add.shard.json.empty"));
            }

            String[] split = shardingNodes.split(",");
            if (split.length > 1 && StringUtils.isEmpty(arg.getShardingRule())) {
                return error(message.get("LogicTableController.add.shardingrule.empty"));
            }

            String rsql = arg.getSql().toLowerCase();
            List<SQLStatement> sts = SQLUtils.parseStatements(rsql, JdbcConstants.MYSQL);
            if (sts.size() != 1) {
                return error(message.get("LogicTableController.add.multi.create.table"));
            }
            SQLStatement st = sts.get(0);
            String shardingColumn = arg.getShardingColumn().toLowerCase();
            if (st instanceof MySqlCreateTableStatement) {
                MySqlCreateTableStatement ct = (MySqlCreateTableStatement) st;
                List<SQLTableElement> colms = ct.getTableElementList();
                boolean checkShardingColInSql = false;
                for (SQLTableElement item : colms) {
                    String name = "";
                    if (item instanceof SQLColumnDefinition) {
                        SQLColumnDefinition col = (SQLColumnDefinition) item;
                        name = col.getName().getSimpleName();
                        name = StringUtil.removeBackquote(name);
                        if (col.isAutoIncrement()) {
                            arg.setAutoIncrement(true);
                        }
                    } else if (item instanceof MySqlPrimaryKey) {
                        MySqlPrimaryKey pk = (MySqlPrimaryKey) item;
                        List<SQLExpr> columns = pk.getColumns();
                        if (columns.size() != 1) {
                            return error(message.get("LogicTableController.add.multi.pk"));
                        }
                        SQLExpr s = columns.get(0);
                        arg.setPrimaryKey(StringUtil.removeBackquote(s.toString()));
                    }
                    if (name.equals(shardingColumn)) {
                        checkShardingColInSql = true;
                    }
                }

                if (checkShardingColInSql == false) {
                    return error(message.get("LogicTableController.add.sharding.column.not.found"));
                }

                List<ShardingNodeInfo> infos = new ArrayList<>(split.length);
                ShardingNode param = new ShardingNode();
                for (String node : split) {
                    param.setName(node);
                    ShardingNode sNode = sDao.get(param);
                    if (sNode == null) {
                        return error(message.get("LogicTableController.add.sharding.node.not.found", node));
                    }

                    String lHost = sNode.getLogicHost();
                    LogicHost lparam = new LogicHost();
                    lparam.setName(lHost);
                    LogicHost logicHost = lServer.get(lparam);
                    if (logicHost == null) {
                        return error(message.get("LogicTableController.add.logic.host.not.found", node, lHost));
                    }

                    List<PhysicsHost> physicsHosts = logicHost.getPhysicsdbs();
                    if (physicsHosts == null || physicsHosts.isEmpty()) {
                        return error(message.get("LogicTableController.add.physics.host.not.found", lHost));
                    }

                    ShardingNodeInfo info = new ShardingNodeInfo();
                    info.physicsHosts = physicsHosts;
                    info.node = sNode;
                    infos.add(info);
                }

                //改写分片数量为节点数量支持dataNode$0-n语法
                String[] dataNodeArr = SplitUtil.split(shardingNodes, ',', '$', '-');
                JSONObject ruleObj = JSONObject.parseObject(arg.getRuleObjJson());
                ruleObj.put("partitionNum", dataNodeArr.length);
                arg.setRuleObjJson(ruleObj.toJSONString());

                SQLExprTableSource tbl = ct.getTableSource();
                SQLExpr expr = tbl.getExpr();
                arg.setName(StringUtil.removeBackquote(expr.toString()));

                MySqlUtils.createTable(arg, infos);
                int add = dbDao.add(arg);

                String info = message.get("LogicTableController.add.scuccess.create.table", arg.getName());
                return json(ERROR, add, info);
            }
        } catch (Exception e) {
            log.error("add err", e);
        }
        return error(message.get("LogicTableController.add.invalid.sql", arg.getSql()));
    }

    @RequestMapping("/table/del")
    @EventFlag(type = Type.CONFIG_RELOAD)
    public Object del(LogicTable arg) {
        int res = dbDao.del(arg);
        return ok(res, (res > 0 ? "success" : "fail"));
    }
}
