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

package io.mycat.web.bean;

import java.util.Date;
import java.util.List;

/**
 * 逻辑主机:将一个或多个Mysql组(主从或单机)从逻辑上划分为一个主机
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月8日 上午10:17:27 
 */
public class LogicHost {

    private Integer           id;

    private String            name;

    private Integer           maxConn;

    private Integer           minConn;

    private String            version;

    private Date              createTime;

    private String            heartbeatSql;

    private String            connectionInitSql;

    /**该主机包含看那几个物理主机*/
    private List<PhysicsHost> physicsdbs;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getConnectionInitSql() {
        return connectionInitSql;
    }

    public void setConnectionInitSql(String connectionInitSql) {
        this.connectionInitSql = connectionInitSql;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxConn() {
        return maxConn;
    }

    public void setMaxConn(Integer maxConn) {
        this.maxConn = maxConn;
    }

    public Integer getMinConn() {
        return minConn;
    }

    public void setMinConn(Integer minConn) {
        this.minConn = minConn;
    }

    public String getHeartbeatSql() {
        return heartbeatSql;
    }

    public void setHeartbeatSql(String heartbeatSql) {
        this.heartbeatSql = heartbeatSql;
    }

    public List<PhysicsHost> getPhysicsdbs() {
        return physicsdbs;
    }

    public void setPhysicsdbs(List<PhysicsHost> physicsdbs) {
        this.physicsdbs = physicsdbs;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
