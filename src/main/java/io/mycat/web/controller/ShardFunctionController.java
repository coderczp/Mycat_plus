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

package io.mycat.web.controller;

import java.util.Collection;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import io.mycat.config.model.rule.RuleAlgorithm;
import io.mycat.web.function.ShardingFunctionFactory;

/**
 * 分区函数管理
 * @author jeff.cao
 * @version 0.0.1, 2018年4月11日 下午10:59:06 
 */
@RestController
public class ShardFunctionController extends BaseController {

    @RequestMapping("/function/list")
    public Object list() {
        Collection<RuleAlgorithm> allFunction = ShardingFunctionFactory.getInstance().getAllFunction();
        return ok(allFunction);
    }

    @RequestMapping("/function/get")
    public Object get(@RequestParam String name) {
        RuleAlgorithm rule = ShardingFunctionFactory.getInstance().getFunctionJson(name);
        JSONObject json = (JSONObject) JSONObject.toJSON(rule);
        json.put("class", rule.getClass());
        return ok(json);
    }

}
