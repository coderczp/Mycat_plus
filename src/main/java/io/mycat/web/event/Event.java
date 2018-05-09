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

/**
 * 事件对象
 * @author jeff.cao
 * @version 0.0.1, 2018年4月12日 上午10:50:09 
 */
public class Event {

    /***
     * 
     * 事件类型,如有需要,可以在这里扩展
     * @author jeff.cao
     * @version 0.0.1, 2018年4月12日 上午10:59:40
     */
    public static enum Type {
                             ANY, CONFIG_RELOAD,
    }

    private Type   type;

    private Object content;

    public Event(Type type, Object content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }

}
