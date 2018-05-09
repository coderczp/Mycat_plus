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

package io.mycat.web.common;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;

import io.mycat.web.controller.BaseController;
import io.mycat.web.event.Event;
import io.mycat.web.event.EventManager;

/**
 * 公共拦截器
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月9日 下午6:52:33 
 */
public class CommonIntercepter implements HandlerInterceptor {

    @Autowired
    private MessageProvider message;

    private static Logger   log = LoggerFactory.getLogger(CommonIntercepter.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse rep, Object handler) throws Exception {
        MessageProvider.setLocale(req.getLocale());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse rep, Object handler,
                                Exception ex) throws Exception {
        try {
            if (ex != null) {
                log.error("error", ex);
                responseError(rep, ex);
            } else {
                sendEventIfRequired(handler);
            }
        } finally {
            MessageProvider.removeLocale();
        }

    }

    private void sendEventIfRequired(Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            EventFlag flag = method.getMethodAnnotation(EventFlag.class);
            if (flag != null) {
                Event event = new Event(flag.type(), null);
                boolean res = EventManager.getInstance().publishEvent(event);
                log.debug("send event:{} retrurn:{}", event, res);
            }
        }
    }

    private void responseError(HttpServletResponse rep, Exception ex) throws IOException {

        Throwable cause = ex.getCause();
        String info = cause.getMessage();
        if (StringUtils.isEmpty(info)) {
            info = message.get("CommonIntercepter.afterCompletion.catch.err");
        }

        JSONObject json = new JSONObject();
        json.put("code", BaseController.ERROR);
        json.put("info", info);

        rep.setContentType(MediaType.APPLICATION_JSON_VALUE);
        rep.setCharacterEncoding("UTF-8");

        PrintWriter writer = rep.getWriter();
        writer.println(json);
        writer.flush();
        writer.close();
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse rep, Object handler,
                           ModelAndView mv) throws Exception {
    }

}
