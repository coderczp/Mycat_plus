package backup.io.mycat.server.handler;
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
 
package io.mycat.server.handler;

import static io.mycat.server.parser.ServerParseSet.AUTOCOMMIT_OFF;
import static io.mycat.server.parser.ServerParseSet.AUTOCOMMIT_ON;
import static io.mycat.server.parser.ServerParseSet.CHARACTER_SET_CLIENT;
import static io.mycat.server.parser.ServerParseSet.CHARACTER_SET_CONNECTION;
import static io.mycat.server.parser.ServerParseSet.CHARACTER_SET_RESULTS;
import static io.mycat.server.parser.ServerParseSet.NAMES;
import static io.mycat.server.parser.ServerParseSet.SESSION_VAR_SET;
import static io.mycat.server.parser.ServerParseSet.TX_READ_COMMITTED;
import static io.mycat.server.parser.ServerParseSet.TX_READ_UNCOMMITTED;
import static io.mycat.server.parser.ServerParseSet.TX_REPEATED_READ;
import static io.mycat.server.parser.ServerParseSet.TX_SERIALIZABLE;
import static io.mycat.server.parser.ServerParseSet.XA_FLAG_OFF;
import static io.mycat.server.parser.ServerParseSet.XA_FLAG_ON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.config.ErrorCode;
import io.mycat.config.Isolations;
import io.mycat.net.mysql.OkPacket;
import io.mycat.net.plus.ClientConn;
import io.mycat.server.NonBlockingSession;
import io.mycat.server.SessionVariable;
import io.mycat.server.parser.ServerParseSet;
import io.mycat.server.response.CharacterSet;
import io.mycat.util.SetIgnoreUtil;

*//**
 * SET 语句处理
 * 
 * @author mycat
 * @author zhuam
 *//*
public final class SetHandler {

    private static final Logger logger = LoggerFactory.getLogger(SetHandler.class);

    private static final byte[] AC_OFF = new byte[] { 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 };

    public static void handle(String stmt, ClientConn c, int offset) {
        // System.out.println("SetHandler: "+stmt);
        int rs = ServerParseSet.parse(stmt, offset);
        int type = rs & 0xff;
        switch (type) {
            case AUTOCOMMIT_ON:
                if (c.isAutocommit()) {
                    c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                } else {
                    c.setPreAcStates(true);
                    c.commit();
                    c.setAutocommit(true);
                }
                break;
            case AUTOCOMMIT_OFF: {
                if (c.isAutocommit()) {
                    c.setAutocommit(false);
                    c.setPreAcStates(false);
                }
                c.write(c.writeToBuffer(AC_OFF, c.allocate()));
                break;
            }
            case XA_FLAG_ON: {
                if (c.isAutocommit()) {
                    c.writeErrMessage((byte)1,ErrorCode.ERR_WRONG_USED, "set xa cmd on can't used in autocommit connection ");
                    return;
                }
                c.getSession2().setXATXEnabled(true);
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            }
            case XA_FLAG_OFF: {
                c.writeErrMessage((byte)1,ErrorCode.ERR_WRONG_USED, "set xa cmd off not for external use ");
                return;
            }
            case TX_READ_UNCOMMITTED: {
                c.setTxIsolation(Isolations.READ_UNCOMMITTED);
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            }
            case TX_READ_COMMITTED: {
                c.setTxIsolation(Isolations.READ_COMMITTED);
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            }
            case TX_REPEATED_READ: {
                c.setTxIsolation(Isolations.REPEATED_READ);
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            }
            case TX_SERIALIZABLE: {
                c.setTxIsolation(Isolations.SERIALIZABLE);
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                break;
            }
            case NAMES:
                String charset = stmt.substring(rs >>> 8).trim();
                int index = charset.indexOf(",");
                if (index > -1) {
                    //支持rails框架自动生成的SET NAMES utf8,  @@SESSION.sql_auto_is_null = 0, @@SESSION.wait_timeout = 2147483, @@SESSION.sql_mode = 'STRICT_ALL_TABLES'
                    charset = charset.substring(0, index);
                }
                if (charset.startsWith("'") && charset.endsWith("'")) {
                    charset = charset.substring(1, charset.length() - 1);
                }
                if (c.setCharset(charset)) {
                    c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                } else {

                    *//**
                     * TODO：修复 phpAyAdmin's 的发包问题
                     * 如： SET NAMES 'utf8' COLLATE 'utf8_general_ci' 错误
                     *//*
                    int beginIndex = stmt.toLowerCase().indexOf("names");
                    int endIndex = stmt.toLowerCase().indexOf("collate");
                    if (beginIndex > -1 && endIndex > -1) {
                        charset = stmt.substring(beginIndex + "names".length(), endIndex);
                        //重试一次
                        if (c.setCharset(charset.trim())) {
                            c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
                        } else {
                            c.writeErrMessage((byte)1,ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
                        }

                    } else {
                        c.writeErrMessage((byte)1,ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
                    }
                }
                break;
            case SESSION_VAR_SET:
                handleCustomSessionVarSet(stmt, c, rs);
                break;
            case CHARACTER_SET_CLIENT:
            case CHARACTER_SET_CONNECTION:
            case CHARACTER_SET_RESULTS:
                CharacterSet.response(stmt, c, rs);
                break;
            default:
                boolean ignore = SetIgnoreUtil.isIgnoreStmt(stmt);
                if (!ignore) {
                    StringBuilder s = new StringBuilder();
                    logger.warn(s.append(c).append(stmt).append(" is not recoginized and ignored").toString());
                }
                c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
        }
    }

    *//**
     * 处理自定义的session会话级变量{@link ServerParseSet.SESSION_VAR_SET}
     * 如:SET SESSION $var={version:defalut}
     * 
     * @param stmt
     * @param c
     * @param rs
     *//*
    private static void handleCustomSessionVarSet(String sql, ClientConn c, int rs) {
        int index = sql.indexOf(ServerParseSet.EXT_SESSION_VAR);
        if (index > -1) {
            //简单的json格式,不用第三方库,这个命令不会频繁执行,性能不会有问题
            String simpleJson = sql.substring(index + 6, sql.lastIndexOf("}"));
            //支持{"version":"xx"}{'version':'xx'}{version:xx}
            String str = simpleJson.replaceAll("[\"|\']", "");
            String[] split = str.split(",");
            NonBlockingSession session = c.getSession2();
            SessionVariable sv = session.getSessionVariable();
            for (String kv : split) {
                String[] kvs = kv.split(":");
                if (kvs.length != 2) {
                    logger.error("invalid Session Variable:{}", sql);
                    c.writeErrMessage((byte)1,ErrorCode.ERR_NOT_SUPPORTED, "invalid Session Variable");
                    return;
                }
                String key = kvs[0];
                String val = kvs[1];
                sv.put(key, val);
            }
            session.switchConfigVersion(sv.getVersion());
            logger.debug("set SessionVariable:{}", sv);
        }
        c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
    }

}
*/