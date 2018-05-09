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

package io.mycat.web.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mycat.web.bean.LogicHost;
import io.mycat.web.bean.PhysicsHost;
import io.mycat.web.bean.PhysicsLogicHostRelation;
import io.mycat.web.dao.LogicHostDao;
import io.mycat.web.dao.PhysicsLogicHostRelationDao;

/**
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月8日 上午10:59:19 
 */
@Service
public class LogicHostService implements LogicHostDao {

    @Autowired
    private LogicHostDao                lDao;

    @Autowired
    private PhysicsLogicHostRelationDao prDao;

    private static Logger               LOG = LoggerFactory.getLogger(LogicHostService.class);

    @Override
    public LogicHost get(LogicHost param) {
        LogicHost logicHost = lDao.get(param);
        if (logicHost != null) {
            PhysicsLogicHostRelation r = new PhysicsLogicHostRelation();
            r.setLogicHostId(logicHost.getId());
            List<PhysicsHost> phs = prDao.queryPhysicsHost(r);
            logicHost.setPhysicsdbs(phs);
        }
        return logicHost;
    }

    @Override
    public int add(LogicHost param) {
        int add = lDao.add(param);
        List<PhysicsHost> pdbs = param.getPhysicsdbs();
        if (add > 0 && pdbs != null) {
            PhysicsLogicHostRelation r = new PhysicsLogicHostRelation();
            for (PhysicsHost item : pdbs) {
                r.setLogicHostId(param.getId());
                r.setPhysicsHostId(item.getId());
                if (prDao.add(r) == 0) {
                    LOG.info("fail to save  PhysicsLogicHostRelation:{}", r);
                }
            }
        }
        return add;
    }

    @Override
    public int del(LogicHost param) {
        int del = lDao.del(param);
        if (del > 0) {
            PhysicsLogicHostRelation r = new PhysicsLogicHostRelation();
            r.setLogicHostId(param.getId());
            int del2 = prDao.del(r);
            LOG.info("delete:{} and del:{} PhysicsLogicHostRelation", param.getName(), del2);
        }
        return del;
    }

    @Override
    public List<LogicHost> listAll() {
        PhysicsLogicHostRelation r = new PhysicsLogicHostRelation();
        List<LogicHost> list = lDao.listAll();
        for (LogicHost item : list) {
            r.setLogicHostId(item.getId());
            List<PhysicsHost> phs = prDao.queryPhysicsHost(r);
            item.setPhysicsdbs(phs);
        }
        return list;
    }

    @Override
    public List<LogicHost> query(LogicHost param) {
        PhysicsLogicHostRelation r = new PhysicsLogicHostRelation();
        List<LogicHost> list = lDao.query(param);
        for (LogicHost item : list) {
            r.setLogicHostId(item.getId());
            List<PhysicsHost> phs = prDao.queryPhysicsHost(r);
            item.setPhysicsdbs(phs);
        }
        return list;
    }

}
