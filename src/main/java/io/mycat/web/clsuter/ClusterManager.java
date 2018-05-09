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

package io.mycat.web.clsuter;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import io.mycat.config.model.MycatNodeConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.web.HostPortManger;
import io.mycat.web.bean.HostPort;
import io.mycat.web.controller.BaseController;
import io.mycat.web.controller.ClusterController;
import io.mycat.web.dao.MycatNodeConfigDao;
import io.mycat.web.util.HttpUtils;

/**
 * 集群管理
 * @author jeff.cao
 * @version 0.0.1, 2018年4月13日 下午5:33:30 
 */
@Component
public class ClusterManager {

    @Autowired
    private MycatNodeConfigDao             mDao;

    private ScheduledExecutorService       pingTimer;

    private MycatNodeConfig                currentNode;

    private SystemConfig                   systemConfig;

    @Value("${cluster.node.ping.period:10000}")
    private long                           pingPeriod = 10000;

    private volatile List<MycatNodeConfig> nodes      = new ArrayList<>();

    private static Logger                  log        = LoggerFactory.getLogger(ClusterManager.class);

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public MycatNodeConfig getCurrentNode() {
        return currentNode;
    }

    @PostConstruct
    public void onStart() {
        pingTimer = Executors.newSingleThreadScheduledExecutor();
        systemConfig = initSystemConfig();
        currentNode = initCurrentNode();
        nodes = loadAllNodes();
        startCheckNodeAlive();
    }

    @PreDestroy
    public void onStop() {
        pingTimer.shutdown();
    }

    public List<MycatNodeConfig> getAllNodes() {
        return nodes;
    }

    private List<MycatNodeConfig> loadAllNodes() {
        MycatNodeConfig param = new MycatNodeConfig();
        return mDao.list(param);
    }

    private void startCheckNodeAlive() {
        pingTimer.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    List<MycatNodeConfig> retryNode = new LinkedList<>();
                    for (MycatNodeConfig node : getAllNodes()) {
                        if (currentNode.getHost().equals(node.getHost()) && currentNode.getPort() == node.getPort()) {
                            continue;
                        }
                        if (isShutdown(node)) {
                            retryNode.add(node);
                        }
                    }

                    //考虑网络容错,如果出错,需要重试
                    List<MycatNodeConfig> delNode = new LinkedList<>();
                    for (MycatNodeConfig node : retryNode) {
                        if (isShutdown(node)) {
                            delNode.add(node);
                        }
                    }
                    //重试后还失败的,直接删除
                    boolean willRefresh = delNode.size() > 0;
                    for (MycatNodeConfig node : delNode) {
                        int del = mDao.delById(node);
                        log.info("ping:{} return error, delete it {}", node, del);
                    }

                    //刷新本地node列表
                    if (willRefresh) {
                        currentNode = initCurrentNode();
                    }
                } catch (Exception e) {
                    log.error("ping error", e);
                }
            }

            private boolean isShutdown(MycatNodeConfig node) {
                JSONObject json = HttpUtils.nodeRpc(node, ClusterController.PING_API);
                return json.getIntValue("code") == BaseController.CONN_REFUSED;
            }
        }, 2, pingPeriod, TimeUnit.MILLISECONDS);

    }

    private MycatNodeConfig initCurrentNode() {
        int weight = 0;
        int port = systemConfig.getServerPort();
        String host = systemConfig.getBindIp();
        String name = System.getProperty("node.name", "node");

        if (host.equals("0.0.0.0")) {
            try {
                host = Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
        HostPort hport = HostPortManger.getWebHostPort();
        MycatNodeConfig node = new MycatNodeConfig();
        node.setWebPort(hport.getPort());
        node.setStartTime(new Date());
        node.setWebPort(port + 1);
        node.setWeight(weight);
        node.setName(name);
        node.setHost(host);
        node.setPort(port);

        MycatNodeConfig info = mDao.get(node);
        if (info != null) {
            mDao.update(node);
        } else {
            mDao.add(node);
        }
        info = node;
        return node;
    }

    private SystemConfig initSystemConfig() {
        int checkGlobleTable = 1;
        HostPort proxy = HostPortManger.getProxyHostPort();
        HostPort mHostPort = HostPortManger.getProxyMangerHostPort();

        SystemConfig system = new SystemConfig();
        system.setBindIp(proxy.getHost());
        system.setUseSqlStat(0);
        //system.setUsingAIO(1);
        system.setUseGlobleTableCheck(0);
        system.setServerPort(proxy.getPort());
        system.setManagerPort(mHostPort.getPort());
        system.setUseGlobleTableCheck(checkGlobleTable);
        system.setFrontSocketNoDelay(true);
        //system.setProcessors(10);

        // 0: DirectByteBufferPool | type 1 ByteBufferArena | type 2 NettyBufferPool
        system.setProcessorBufferPoolType(2);
        system.setBufferPoolChunkSize((short) 8192);
        //System.setSequnceHandlerType(SystemConfig.SEQUENCEHANDLER_LOCALFILE);
        system.setSequnceHandlerType(SystemConfig.SNOWFLAKE_SEQUENCEHANDLER);
        return system;
    }

}
