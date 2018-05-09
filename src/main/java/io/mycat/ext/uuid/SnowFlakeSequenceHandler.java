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

package io.mycat.ext.uuid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.config.model.ClusterConfig;
import io.mycat.config.model.MycatNodeConfig;
import io.mycat.route.sequence.handler.SequenceHandler;

/**
 * 雪花算法自增,延迟加载的单列
 * @author jeff.cao
 * @version 0.0.1, 2018年4月12日 下午6:46:32 
 */
public class SnowFlakeSequenceHandler implements SequenceHandler {

    private static class LazySingle {
        private static final SnowFlakeSequenceHandler instance = new SnowFlakeSequenceHandler();
    }

    private static final Logger LOG = LoggerFactory.getLogger(SnowFlakeSequenceHandler.class);

    private SnowFlakeUUID       uuid;

    private SnowFlakeSequenceHandler() {
        MycatConfig config = MycatServer.getInstance().getConfig();
        ClusterConfig cluster = config.getConfigIniter().getConfigLoader().getClusterConfig();
        MycatNodeConfig currentNode = cluster.getCurrentNode();
        long datacenterId = currentNode.getRegion();
        long machineId = currentNode.getId();
        uuid = new SnowFlakeUUID(datacenterId, machineId);
        LOG.info("init uuid handler,datacenterId:{},machineId:{}", datacenterId, machineId);
    }

    @Override
    public long nextId(String prefixName) {
        return uuid.nextId();
    }

    public static SnowFlakeSequenceHandler getInstance() {
        return LazySingle.instance;
    }

}
