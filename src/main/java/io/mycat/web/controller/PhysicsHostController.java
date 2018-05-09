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
import org.springframework.web.bind.annotation.RestController;

import io.mycat.web.bean.PhysicsHost;
import io.mycat.web.common.EventFlag;
import io.mycat.web.dao.PhysicsHostDao;
import io.mycat.web.event.Event.Type;

/**
 * API 
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月5日 下午11:45:39 
 */
@RestController
public class PhysicsHostController extends BaseController {

    @Autowired
    private PhysicsHostDao infoDao;

    @RequestMapping(value = "/physicsHost/list")
    public Object list(PhysicsHost arg) {
        List<PhysicsHost> list = infoDao.list(arg);
        for (PhysicsHost mysqlInfo : list) {
            mysqlInfo.setPassword("******");
        }
        return ok(list);
    }

    @RequestMapping("/physicsHost/add")
    @EventFlag(type = Type.CONFIG_RELOAD)
    public Object add(PhysicsHost arg) {
        int res = infoDao.add(arg);
        return ok(res, (res > 0 ? "success" : "fail"));
    }

    @RequestMapping("/physicsHost/del")
    @EventFlag(type = Type.CONFIG_RELOAD)
    public Object del(PhysicsHost arg) {
        int res = infoDao.del(arg);
        return ok(res, (res > 0 ? "success" : "fail"));
    }
}
