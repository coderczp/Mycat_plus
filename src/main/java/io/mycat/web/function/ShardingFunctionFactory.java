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

package io.mycat.web.function;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

import io.mycat.config.model.rule.RuleAlgorithm;
import io.mycat.route.function.PartitionByHashMod;
import io.mycat.route.function.PartitionByMod;
import io.mycat.route.function.custom.SubstringHashMod;

/**
 * 分片函数工厂
 * @author jeff.cao
 * @version 0.0.1, 2018年4月11日 下午9:51:10 
 */
public class ShardingFunctionFactory {

    private static final ShardingFunctionFactory INSTANCE  = new ShardingFunctionFactory();
    private Map<String, RuleAlgorithm>           functions = new ConcurrentHashMap<>();

    private ShardingFunctionFactory() {
        regist(new PartitionByMod());
        regist(new SubstringHashMod());
        regist(new PartitionByHashMod());
    }

    public static ShardingFunctionFactory getInstance() {
        return INSTANCE;
    }

    /***
     * 注册分片函数
     * 
     * @param rule
     * @return
     */
    public boolean regist(RuleAlgorithm rule) {
        return functions.put(rule.getName(), rule) != null;
    }

    /***
     * 加载扩展的分片函数
     */
    public static void loadExtendFunction() {

    }

    /**
     * 获取所有的分片函数名
     * 
     * @return
     */
    public Collection<RuleAlgorithm> getAllFunction() {
        return functions.values();
    }

    /***
     * 获取分片函数的对象
     * 
     * @param functionName
     * @param tableNodeCount
     * @return
     */
    public RuleAlgorithm getFunctionJson(String functionName) {
        RuleAlgorithm class1 = functions.get(functionName);
        Assert.notNull(class1, String.format("can't find function of name:%s", functionName));
        return class1;
    }
}
