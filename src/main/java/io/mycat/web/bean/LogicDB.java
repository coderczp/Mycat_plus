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

import java.sql.Date;

/**
 * schema.xml里的schema
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月8日 下午3:41:40 
 */
public class LogicDB {

    private Integer id;

    private String  name;

    private String  dataNode;

    private boolean checkSQLschema;

    private Integer sqlMaxLimit;

    private Date    createTime;

    private String  version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDataNode() {
        return dataNode;
    }

    public void setDataNode(String dataNode) {
        this.dataNode = dataNode;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCheckSQLschema() {
        return checkSQLschema;
    }

    public void setCheckSQLschema(boolean checkSQLschema) {
        this.checkSQLschema = checkSQLschema;
    }

    public Integer getSqlMaxLimit() {
        return sqlMaxLimit;
    }

    public void setSqlMaxLimit(Integer sqlMaxLimit) {
        this.sqlMaxLimit = sqlMaxLimit;
    }

    /** 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogicDB [id=" + id + ", name=" + name + ", dataNode=" + dataNode + ", checkSQLschema=" + checkSQLschema
               + ", sqlMaxLimit=" + sqlMaxLimit + ", createTime=" + createTime + ", version=" + version + "]";
    }

}
