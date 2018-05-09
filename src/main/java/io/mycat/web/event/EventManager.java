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

package io.mycat.web.event;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.web.event.Event.Type;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月12日 上午10:42:46 
 */
public class EventManager {

    private ConcurrentHashMap<Event.Type, Set<EventHandler>> maps     = new ConcurrentHashMap<>();

    private static final EventManager                        INSTANCE = new EventManager();

    private static final Logger                              LOG      = LoggerFactory.getLogger(EventManager.class);

    private EventManager() {
    }

    public static EventManager getInstance() {
        return INSTANCE;
    }

    public boolean registHandler(EventHandler handler) {
        Type type = handler.type();
        Set<EventHandler> list = maps.get(type);
        if (list == null) {
            maps.putIfAbsent(type, new CopyOnWriteArraySet<EventHandler>());
        }
        return maps.get(type).add(handler);
    }

    public boolean publishEvent(Event event) {
        if (event.getType() == Type.ANY) {
            return fireAllHandler(event);
        } else {
            return fireSpecHandler(event);
        }
    }

    private boolean fireSpecHandler(Event event) {
        Type type = event.getType();
        Set<EventHandler> list = maps.get(type);
        if (list == null || list.isEmpty()) {
            LOG.info("can't found Hander, type:{}", type);
            return false;
        }
        for (EventHandler eventHandler : list) {
            eventHandler.onEvent(event);
        }
        return false;
    }

    private boolean fireAllHandler(Event event) {
        LOG.debug("will send {} handler", maps.size());
        for (Entry<Type, Set<EventHandler>> entry : maps.entrySet()) {
            for (EventHandler eventHandler : entry.getValue()) {
                eventHandler.onEvent(event);
            }
        }
        return true;
    }

}
