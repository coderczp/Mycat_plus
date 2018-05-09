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

import java.util.Date;

/**
 * 雪花算法UUID
 * @author jeff.cao coder_czp@126.com
 * @version 0.0.1, 2018年4月10日 下午11:32:41 
 */
public class SnowFlakeUUID{

    /**
    * 起始的时间戳2018-4-11
    */
    @SuppressWarnings("deprecation")
    private final static long START_STMP         = new Date(2018 - 1900, 3, 10).getTime();

    /**
    * 每一部分占用的位数
    */
    private final static long SEQUENCE_BIT       = 12;                                    //序列号占用的位数
    private final static long MACHINE_BIT        = 5;                                     //机器标识占用的位数
    private final static long DATACENTER_BIT     = 5;                                     //数据中心占用的位数

    /**
    * 每一部分的最大值
    */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM    = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE       = -1L ^ (-1L << SEQUENCE_BIT);

    /**
    * 每一部分向左的位移
    */
    private final static long MACHINE_LEFT       = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT    = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT      = DATACENTER_LEFT + DATACENTER_BIT;

    private long              datacenterId;                                               //数据中心
    private long              machineId;                                                  //机器标识
    private long              sequence           = 0L;                                    //序列号
    private long              lastStmp           = -1L;                                   //上一次时间戳

    public SnowFlakeUUID(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
    * 产生下一个ID
    *
    * @return
    */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
               | datacenterId << DATACENTER_LEFT //数据中心部分
               | machineId << MACHINE_LEFT //机器标识部分
               | sequence; //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

}
