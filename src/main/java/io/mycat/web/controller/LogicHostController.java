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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import io.mycat.web.bean.LogicHost;
import io.mycat.web.bean.PhysicsHost;
import io.mycat.web.common.EventFlag;
import io.mycat.web.event.Event.Type;
import io.mycat.web.service.LogicHostService;

/**
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月8日 上午11:38:57 
 */
@RestController
public class LogicHostController extends BaseController {

    @Autowired
    private LogicHostService logicService;

    @RequestMapping(value = "/logicHost/list")
    public Object list(LogicHost arg) {
        List<LogicHost> list = logicService.query(arg);
        for (LogicHost mysqlInfo : list) {
            mysqlInfo.setHeartbeatSql(null);
            for (PhysicsHost item : mysqlInfo.getPhysicsdbs()) {
                item.setPassword("******");
            }
        }
        return ok(list);
    }

    @RequestMapping("/logicHost/add")
    @EventFlag(type = Type.CONFIG_RELOAD)
    public Object add(@RequestParam String arg) {
        LogicHost host = JSONObject.parseObject(arg, LogicHost.class);
        int res = logicService.add(host);
        return ok(res, (res > 0 ? "success" : "fail"));
    }

    @RequestMapping("/logicHost/del")
    @EventFlag(type = Type.CONFIG_RELOAD)
    public Object del(LogicHost arg) {
        int res = logicService.del(arg);
        return ok(res, (res > 0 ? "success" : "fail"));
    }
}
