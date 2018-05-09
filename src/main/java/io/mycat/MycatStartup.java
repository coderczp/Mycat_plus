/*
 * Copyright (c) 2013, MyCat_Plus and/or its affiliates. All rights reserved.
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
package io.mycat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.config.loader.ConfigLoader;
import io.mycat.config.loader.xml.XMLConfigLoader;
import io.mycat.config.loader.xml.XMLSchemaLoader;
import io.mycat.config.model.SystemConfig;
import io.mycat.web.WebStarter;

/**
 * @author mycat
 */
public final class MycatStartup {

    private static final Logger LOGGER = LoggerFactory.getLogger(MycatStartup.class);

    public static void main(String[] args) {
        try {
            if (System.getProperty("spring.profiles.active") == null) {
                System.setProperty("spring.profiles.active", "local");
            }
            
            System.out.println(System.getProperty("spring.profiles.active"));

            String home = SystemConfig.getHomePath();
            if (home == null) {
                System.out.println(SystemConfig.SYS_HOME + "  is not set.");
                System.exit(-1);
            }

            final WebStarter m = new WebStarter(home);
            m.doStart();

            ConfigLoader configLoader = null;
            configLoader = m.getBean(ConfigLoader.class);
            if (configLoader == null) {
                configLoader = useXmlConfigLoader();
            }

            final MycatServer server = MycatServer.getInstance();
            server.init(configLoader);
            server.startup();
            System.out.println("MyCAT Server startup successfully. see logs in logs/mycat.log");
            m.join();

        } catch (Exception e) {
            LOGGER.error("startup error", e);
            System.exit(-1);
        }
    }

    private static ConfigLoader useXmlConfigLoader() {
        //读取schema.xml，rule.xml和server.xml
        return new XMLConfigLoader(new XMLSchemaLoader());
    }
}
