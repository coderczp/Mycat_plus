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

/**
 * 逻辑主机和物理主机的映射关系,一个逻辑主机可以配置多个物理主机
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月8日 上午10:38:56 
 */
public class PhysicsLogicHostRelation {

    private Integer id;

    private Integer logicHostId;

    private Integer physicsHostId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLogicHostId() {
        return logicHostId;
    }

    public void setLogicHostId(Integer logicHostId) {
        this.logicHostId = logicHostId;
    }

    public Integer getPhysicsHostId() {
        return physicsHostId;
    }

    public void setPhysicsHostId(Integer physicsHostId) {
        this.physicsHostId = physicsHostId;
    }

    @Override
    public String toString() {
        return "PhysicsLogicHostRelation [logicHostId=" + logicHostId + ", physicsHostId=" + physicsHostId + "]";
    }

}
