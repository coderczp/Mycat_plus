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
 * 逻辑表
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月8日 下午3:35:18 
 */
public class LogicTable {

    private Integer id;

    private String  sql;

    private String  name;

    /**表类型,1:全局表 0普通表*/
    private int     tableType;

    /**主键是否自增*/
    private boolean autoIncrement;

    private String  version;

    private String  logicDb;

    private String  primaryKey;

    private String  shardingRule;

    private String  shardingNodes;

    private String  shardingColumn;

    /**规则对象的json序列化,对象的属性是不固定的,所以用json*/
    private String  ruleObjJson;

    private Date    createTime;

    public String getRuleObjJson() {
        return ruleObjJson;
    }

    public void setRuleObjJson(String ruleObjJson) {
        this.ruleObjJson = ruleObjJson;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public int getTableType() {
        return tableType;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
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

    public String getLogicDb() {
        return logicDb;
    }

    public void setLogicDb(String logicDb) {
        this.logicDb = logicDb;
    }

    public String getShardingRule() {
        return shardingRule;
    }

    public void setShardingRule(String shardingRule) {
        this.shardingRule = shardingRule;
    }

    public String getShardingNodes() {
        return shardingNodes;
    }

    public void setShardingNodes(String shardingNodes) {
        this.shardingNodes = shardingNodes;
    }

    public String getShardingColumn() {
        return shardingColumn;
    }

    public void setShardingColumn(String shardingColumn) {
        this.shardingColumn = shardingColumn;
    }

}
