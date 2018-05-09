/*
 * Copyright (c) 2013, MyCat_Plus and/or its affiliates. All rights reserved.
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
package io.mycat.server;

import io.mycat.backend.BackendConnection;
import io.mycat.backend.mysql.nio.handler.MiddlerResultHandler;
import io.mycat.config.MycatConfig;
import io.mycat.net.plus.ClientConn;
import io.mycat.route.RouteResultset;
import io.mycat.route.RouteResultsetNode;

/**
 * @author mycat
 */
public interface Session {

    /**
     * 获取当前会话的扩展信息
     * 
     * @return
     */
    SessionVariable getSessionVariable();

    /***
     * 获取SQL语法树
     * 
     * @return
     */
    SqlContext getSqlCtx();

    /**
     * 取得源端连接
     */
    ClientConn getSource();

    /**
     * 取得当前目标端数量
     */
    int getTargetCount();

    /**
     * 开启一个会话执行
     */
    void execute(RouteResultset rrs, int type);

    /**
     * 提交一个会话执行
     */
    void commit();

    /**
     * 回滚一个会话执行
     */
    void rollback();

    /**
     * 取消一个正在执行中的会话
     * 
     * @param sponsor
     *            如果发起者为null，则表示由自己发起。
     */
    void cancel(ClientConn sponsor);

    /**
     * 终止会话，必须在关闭源端连接后执行该方法。
     */
    void terminate();

    /**
     * 当前会话的配置
     * @return
     */
    MycatConfig getCurrentConfig();

    /**
     * 
     * @return
     */
    String getXaTXID();

    /**
     * 
     * @return
     */
    MiddlerResultHandler<?> getMiddlerResultHandler();

    /**
     * 
     * @param b
     */
    void setPrepared(boolean b);

    /**
     * 
     * @param conn
     * @param node
     * @return
     */
    boolean tryExistsCon(BackendConnection conn, RouteResultsetNode node);

    /**
     * 
     * @param object
     */
    void setMiddlerResultHandler(MiddlerResultHandler<?> middlerResultHandler);

    /**
     * 
     * @param b
     */
    void setCanClose(boolean b);

    /**
     * 
     * @return
     */
    boolean isPrepared();

}