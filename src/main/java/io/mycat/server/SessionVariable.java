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

package io.mycat.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当前会话的相关变量{@link https://dev.mysql.com/doc/refman/5.7/en/using-system-variables.html}
 * 
 * @author jeff.cao
 * @version 0.0.1, 2018年4月12日 下午4:40:36 
 */
public class SessionVariable {

    private static ThreadLocal<Map<String, String>> version   = new ThreadLocal<>();

    private ConcurrentHashMap<String, String>       variables = new ConcurrentHashMap<>();

    public SessionVariable() {
        variables.put("version", "default");
        version.set(variables);
    }

    public static Map<String, String> getSession() {
        return version.get();
    }

    public String getVersion() {
        return variables.get("version");
    }

    public String getValue(String variable) {
        return variables.get(variable);
    }

    public Long getLongValue(String variable) {
        String val = variables.get(variable);
        return val == null ? null : Long.parseLong(val);
    }

    public Integer getIntValue(String variable) {
        String val = variables.get(variable);
        return val == null ? null : Integer.getInteger(val);
    }

    public boolean put(String variable, String val) {
        return variables.put(variable, val) != null;
    }

    @Override
    public String toString() {
        return "SessionVariable:" + variables;
    }

}
