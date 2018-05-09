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

package io.mycat.server.handler.plus;

import java.util.concurrent.ConcurrentHashMap;

import io.mycat.server.handler.plus.impl.BeginHandler;
import io.mycat.server.handler.plus.impl.CmdCommentHandler;
import io.mycat.server.handler.plus.impl.CommentHandler;
import io.mycat.server.handler.plus.impl.CommitHandler;
import io.mycat.server.handler.plus.impl.ExplainHandler;
import io.mycat.server.handler.plus.impl.HelpHandler;
import io.mycat.server.handler.plus.impl.KillHandler;
import io.mycat.server.handler.plus.impl.KillQueryHandler;
import io.mycat.server.handler.plus.impl.LoadDataHandler;
import io.mycat.server.handler.plus.impl.LockTableHandler;
import io.mycat.server.handler.plus.impl.MyCatExplainHandler;
import io.mycat.server.handler.plus.impl.ReadOnlyHandler;
import io.mycat.server.handler.plus.impl.RollbackHandler;
import io.mycat.server.handler.plus.impl.SavepointHandler;
import io.mycat.server.handler.plus.impl.SelectHandler;
import io.mycat.server.handler.plus.impl.ShowCache;
import io.mycat.server.handler.plus.impl.ShowHandler;
import io.mycat.server.handler.plus.impl.StartHandler;
import io.mycat.server.handler.plus.impl.UnLockTableHandler;
import io.mycat.server.handler.plus.impl.UseHandler;

/**
 * 处理器管理
 * @author jeff.cao
 * @version 0.0.1, 2018年4月26日 上午11:30:59 
 */
public class SQLHandlerManager {

    private static ConcurrentHashMap<Integer, SQLHandler> handlers  = new ConcurrentHashMap<>();

    public static final SQLHandler                        READ_ONLY = new ReadOnlyHandler();

    static {
        regist(new BeginHandler());
        regist(new CommentHandler());
        regist(new CommitHandler());
        regist(new CmdCommentHandler());
        regist(new ExplainHandler());
        regist(new HelpHandler());
        regist(new KillHandler());
        regist(new KillQueryHandler());
        regist(new LoadDataHandler());
        regist(new MyCatExplainHandler());
        regist(new LockTableHandler());
        regist(new RollbackHandler());
        regist(new SavepointHandler());
        regist(new SelectHandler());
        regist(new ShowCache());
        regist(new ShowHandler());
        regist(new StartHandler());
        regist(new UnLockTableHandler());
        regist(new UseHandler());
    }

    /***
     * 注册SQL处理器
     * 
     * @param handler
     * @return
     */
    public final static boolean regist(SQLHandler handler) {
        return handlers.put(handler.type(), handler) != null;
    }

    /***
     * 获取指定类型的处理器
     * 
     * @param type
     * @return
     */
    public final static SQLHandler get(int type) {
        return handlers.get(type);
    }
}
