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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

import io.mycat.MycatServer;

/**
 * web模块
 * @author jeff.cao
 * @version 0.0.1, 2018年4月5日 下午11:37:29 
 */
public class WebModule {

    private int                 webPort;
    private Server              server      = new Server();

    private static final String SPRING_FILE = "classpath:spring/spring.xml";
    private static final Logger LOG         = LoggerFactory.getLogger(WebModule.class);

    public WebModule() {

        webPort = Integer.parseInt(System.getProperty("web.port", "8087"));
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(webPort);
        connector.setReuseAddress(false);
        server.setConnectors(new Connector[] { connector });

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("web");
        server.setHandler(context);

        DispatcherServlet spring = new DispatcherServlet();
        ServletHolder holder = new ServletHolder(spring);
        holder.setInitParameter("contextConfigLocation", SPRING_FILE);
        context.addServlet(holder, "/");

    }

    public void doStart(MycatServer mycat) throws Exception {
        server.setStopAtShutdown(true);
        server.start();
        server.join();
        LOG.info("WebModule started");
    }

    public void doStop(MycatServer mycat) throws Exception {
        server.stop();
        LOG.info("WebModule stoped");
    }
}
