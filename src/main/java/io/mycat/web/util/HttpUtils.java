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

package io.mycat.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSONObject;

import io.mycat.config.model.MycatNodeConfig;
import io.mycat.web.controller.BaseController;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月14日 上午10:56:26 
 */
public class HttpUtils {

    private final static Log LOG = LogFactory.getLog(HttpUtils.class);

    public static JSONObject getJson(String url, int timeout) {
        return JSONObject.parseObject(get(url, timeout));
    }

    public static String get(String url, int timeout) {
        StringBuilder resp = new StringBuilder();
        BufferedReader reader = null;
        URL urlObj;
        try {
            urlObj = new URL(url);
            URLConnection conn = urlObj.openConnection();
            conn.setConnectTimeout(timeout * 1000);
            conn.setDoOutput(true);

            String line = null;
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }
        } catch (ConnectException ec) {
            JSONObject json = new JSONObject();
            json.put("code", BaseController.CONN_REFUSED);
            json.put("info", ec.getMessage());
            return json.toJSONString();
        } catch (IOException e) {
            LOG.error("request err:" + url, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("close error", e);
                }
            }
        }
        return resp.toString();
    }

    /**
     * 调用远程节点
     * @param node
     * @return
     */
    public static JSONObject nodeRpc(MycatNodeConfig node, String method) {
        String url = String.format("http://%s:%s/%s", node.getHost(), node.getWebPort(), method);
        return HttpUtils.getJson(url, 10);
    }

}
