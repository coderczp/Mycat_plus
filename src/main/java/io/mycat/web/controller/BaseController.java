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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import io.mycat.web.common.MessageProvider;

/**
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月7日 下午1:55:13 
 */
@RequestMapping(path = "/api/", produces = "application/json; charset=UTF-8")
public class BaseController {

    public static final int   OK           = 200;
    public static final int   ERROR        = 500;
    public static final int   CONN_REFUSED = 1500;

    @Autowired
    protected MessageProvider message;

    public static JSONObject ok(Object data) {
        return json(OK, data, "");
    }

    public static JSONObject error(String info) {
        return json(ERROR, null, info);
    }

    public static JSONObject ok(Object data, String info) {
        return json(OK, data, info);
    }

    public static JSONObject json(int code, Object data, String info) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("info", info);
        json.put("data", data);
        return json;
    }
}
