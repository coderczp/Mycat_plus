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

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mycat.web.bean.LogicHost;
import io.mycat.web.bean.LogicTable;
import io.mycat.web.bean.ShardingNode;
import io.mycat.web.common.EventFlag;
import io.mycat.web.dao.LogicTableDao;
import io.mycat.web.dao.ShardingNodeDao;
import io.mycat.web.event.Event;
import io.mycat.web.event.Event.Type;
import io.mycat.web.event.EventManager;
import io.mycat.web.service.LogicHostService;
import io.mycat.web.util.MySqlUtils;

/**
 * API 
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月5日 下午11:45:39 
 */
@RestController
public class DataNodeController extends BaseController {

    @Autowired
    private ShardingNodeDao  infoDao;

    @Autowired
    private LogicHostService lhDao;

    @Autowired
    private LogicTableDao    tblDao;

    @RequestMapping(value = "/dataNode/list")
    public Object list(ShardingNode arg) {
        List<ShardingNode> list = infoDao.list(arg);
        return ok(list);
    }

    @RequestMapping("/dataNode/add")
    @EventFlag(type = Type.CONFIG_RELOAD)
    public Object add(ShardingNode arg) throws SQLException {
        if (arg.isAutoCreate()) {
            LogicHost param = new LogicHost();
            param.setName(arg.getLogicHost());
            LogicHost host = lhDao.get(param);
            MySqlUtils.createDB(arg, host);
        }
        int add = infoDao.add(arg);
        return ok(add, (add > 0 ? "success" : "fail"));
    }

    @RequestMapping("/dataNode/del")
    public Object del(ShardingNode arg) {
        ShardingNode node = infoDao.get(arg);
        if (node == null) {
            return error(message.get("DataNodeController.del.node.not.found"));
        }
        
        String name = node.getName();
        List<LogicTable> list = tblDao.list(null);
        for (LogicTable tbl : list) {
            String tName = tbl.getName();
            if (tbl.getShardingNodes().contains(name)) {
                return error(message.get("DataNodeController.del.node.related", name, tName));
            }
        }
        int del = infoDao.del(node);
        if (del > 0) {
            Event event = new Event(Event.Type.CONFIG_RELOAD, arg);
            EventManager.getInstance().publishEvent(event);
            return ok(del, "success");
        }
        return ok(del, "fail");
    }
}
