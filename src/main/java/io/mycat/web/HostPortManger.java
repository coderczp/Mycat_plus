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

package io.mycat.web;

import java.util.Properties;

import io.mycat.web.bean.HostPort;

/**
 * 加载配置端口
 * @author jeff.cao
 * @version 0.0.1, 2018年4月13日 下午6:11:07 
 */
public class HostPortManger {

    private static HostPort web;
    private static HostPort proxy;
    private static HostPort proxyManager;

    public static HostPort getWebHostPort() {
        if (web == null) {
            web = buildWithSystemProp("web", "0.0.0.0", "8067");
        }
        return web;
    }

    public static HostPort getProxyHostPort() {
        if (proxy == null) {
            proxy = buildWithSystemProp("proxy", "0.0.0.0", "8066");
        }
        return proxy;
    }

    public static HostPort getProxyMangerHostPort() {
        if (proxyManager == null) {
            proxyManager = buildWithSystemProp("proxy.manger", "0.0.0.0", "9066");
        }
        return proxyManager;
    }

    public static HostPort buildWithSystemProp(String key, String defHost, String defPort) {
        return build(System.getProperties(), key, defHost, defPort);
    }

    public static HostPort build(Properties prop, String key, String defHost, String defPort) {
        int port = Integer.parseInt(System.getProperty(key + ".port", defPort));
        String host = System.getProperty(key + ".ip", defHost);
        return new HostPort(port, host);
    }
}
