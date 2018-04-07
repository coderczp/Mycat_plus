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

package io.mycat.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import io.mycat.web.bean.MysqlInfo;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月6日 下午8:40:14 
 */
public interface MysqlInfoDao {

    @Select("select * from mysql_info")
    List<MysqlInfo> list(MysqlInfo param);

    @Select("select * from mysql_info where pid=#{pid}")
    List<MysqlInfo> querySlaves(MysqlInfo param);

    @Select("select * from mysql_info where id=#{id}")
    MysqlInfo get(MysqlInfo param);

    @Insert("insert into mysql_info(`pid`,`name`,`host`,`port`,`user`,`password`,`type`)"
            + "values(#{pid},#{name},#{host},#{port},#{user},#{password},#{type})")
    int add(MysqlInfo param);

    @Delete("delete from mysql_info where id=#{id}")
    int del(MysqlInfo param);
}
