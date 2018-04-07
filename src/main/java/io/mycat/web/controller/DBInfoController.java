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

import io.mycat.web.bean.DatabaseInfo;
import io.mycat.web.dao.DatebaseInfoDao;

/**
 * API 
 * @author jeff.cao
 * @version 0.0.1, 2018年4月5日 下午11:45:39 
 */
@RestController
public class DBInfoController extends BaseController {

    @Autowired
    private DatebaseInfoDao infoDao;

    @RequestMapping(value = "/db/list")
    public List<DatabaseInfo> list(DatabaseInfo arg) {
        List<DatabaseInfo> list = infoDao.list(arg);
        return list;
    }

    @RequestMapping("/db/add")
    public Object add(DatabaseInfo arg) {
        return infoDao.add(arg);
    }

    @RequestMapping("/db/del")
    public Object del(DatabaseInfo arg) {
        return infoDao.del(arg);
    }
}
