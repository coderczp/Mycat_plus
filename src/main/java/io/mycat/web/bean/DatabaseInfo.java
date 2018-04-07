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

/**
 * 逻辑库
 * @author jeff.cao
 * @version 0.0.1, 2018年4月7日 下午8:45:06 
 */
public class DatabaseInfo {

    private Integer id;

    private String  logicdb;

    private String  physicsdb;

    private Date    createTime;

    private String  mysqlInfoName;

    public String getMysqlInfoName() {
        return mysqlInfoName;
    }

    public void setMysqlInfoName(String mysqlInfoName) {
        this.mysqlInfoName = mysqlInfoName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogicdb() {
        return logicdb;
    }

    public void setLogicdb(String logicdb) {
        this.logicdb = logicdb;
    }

    public String getPhysicsdb() {
        return physicsdb;
    }

    public void setPhysicsdb(String physicsdb) {
        this.physicsdb = physicsdb;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
