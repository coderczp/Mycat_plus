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
package io.mycat.parser;

import org.junit.Assert;
import org.junit.Test;

import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.parser.SimpleSqlParser;
import io.mycat.server.parser.ServerParseSelect;
import io.mycat.server.parser.ServerParseSet;
import io.mycat.server.parser.ServerParseShow;
import io.mycat.server.parser.ServerParseStart;

/**
 * @author mycat
 */
public class ServerParserTest {

    @Test
    public void testIsBegin() {
        Assert.assertEquals(SQLHandler.Type.BEGIN, SimpleSqlParser.parse("begin"));
        Assert.assertEquals(SQLHandler.Type.BEGIN, SimpleSqlParser.parse("BEGIN"));
        Assert.assertEquals(SQLHandler.Type.BEGIN, SimpleSqlParser.parse("BegIn"));
    }

    @Test
    public void testIsCommit() {
        Assert.assertEquals(SQLHandler.Type.COMMIT, SimpleSqlParser.parse("commit"));
        Assert.assertEquals(SQLHandler.Type.COMMIT, SimpleSqlParser.parse("COMMIT"));
        Assert.assertEquals(SQLHandler.Type.COMMIT, SimpleSqlParser.parse("cOmmiT "));
    }
    

    @Test
    public void testComment() {
        Assert.assertEquals(SQLHandler.Type.MYSQL_CMD_COMMENT, SimpleSqlParser.parse("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */"));
        Assert.assertEquals(SQLHandler.Type.MYSQL_CMD_COMMENT, SimpleSqlParser.parse("/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */"));
        Assert.assertEquals(SQLHandler.Type.MYSQL_CMD_COMMENT, SimpleSqlParser.parse("/*!40101 SET @saved_cs_client     = @@character_set_client */"));
   
        Assert.assertEquals(SQLHandler.Type.MYSQL_COMMENT, SimpleSqlParser.parse("/*SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */"));
        Assert.assertEquals(SQLHandler.Type.MYSQL_COMMENT, SimpleSqlParser.parse("/*SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */"));
        Assert.assertEquals(SQLHandler.Type.MYSQL_COMMENT, SimpleSqlParser.parse("/*SET @saved_cs_client     = @@character_set_client */"));
    }

    @Test
    public void testMycatComment() {
        Assert.assertEquals(SQLHandler.Type.SELECT, 0xff & SimpleSqlParser.parse("/*#mycat:schema=DN1*/SELECT ..."));
        Assert.assertEquals(SQLHandler.Type.UPDATE, 0xff & SimpleSqlParser.parse("/*#mycat: schema = DN1 */ UPDATE ..."));
        Assert.assertEquals(SQLHandler.Type.DELETE, 0xff & SimpleSqlParser.parse("/*#mycat: sql = SELECT id FROM user */ DELETE ..."));
    }

    @Test
    public void testOldMycatComment() {
        Assert.assertEquals(SQLHandler.Type.SELECT, 0xff & SimpleSqlParser.parse("/*!mycat:schema=DN1*/SELECT ..."));
        Assert.assertEquals(SQLHandler.Type.UPDATE, 0xff & SimpleSqlParser.parse("/*!mycat: schema = DN1 */ UPDATE ..."));
        Assert.assertEquals(SQLHandler.Type.DELETE, 0xff & SimpleSqlParser.parse("/*!mycat: sql = SELECT id FROM user */ DELETE ..."));
    }

    @Test
    public void testIsDelete() {
        Assert.assertEquals(SQLHandler.Type.DELETE, SimpleSqlParser.parse("delete ..."));
        Assert.assertEquals(SQLHandler.Type.DELETE, SimpleSqlParser.parse("DELETE ..."));
        Assert.assertEquals(SQLHandler.Type.DELETE, SimpleSqlParser.parse("DeletE ..."));
    }

    @Test
    public void testIsInsert() {
        Assert.assertEquals(SQLHandler.Type.INSERT, SimpleSqlParser.parse("insert ..."));
        Assert.assertEquals(SQLHandler.Type.INSERT, SimpleSqlParser.parse("INSERT ..."));
        Assert.assertEquals(SQLHandler.Type.INSERT, SimpleSqlParser.parse("InserT ..."));
    }

    @Test
    public void testIsReplace() {
        Assert.assertEquals(SQLHandler.Type.REPLACE, SimpleSqlParser.parse("replace ..."));
        Assert.assertEquals(SQLHandler.Type.REPLACE, SimpleSqlParser.parse("REPLACE ..."));
        Assert.assertEquals(SQLHandler.Type.REPLACE, SimpleSqlParser.parse("rEPLACe ..."));
    }

    @Test
    public void testIsRollback() {
        Assert.assertEquals(SQLHandler.Type.ROLLBACK, SimpleSqlParser.parse("rollback"));
        Assert.assertEquals(SQLHandler.Type.ROLLBACK, SimpleSqlParser.parse("ROLLBACK"));
        Assert.assertEquals(SQLHandler.Type.ROLLBACK, SimpleSqlParser.parse("rolLBACK "));
    }

    @Test
    public void testIsSelect() {
        Assert.assertEquals(SQLHandler.Type.SELECT, 0xff & SimpleSqlParser.parse("select ..."));
        Assert.assertEquals(SQLHandler.Type.SELECT, 0xff & SimpleSqlParser.parse("SELECT ..."));
        Assert.assertEquals(SQLHandler.Type.SELECT, 0xff & SimpleSqlParser.parse("sELECt ..."));
    }

    @Test
    public void testIsSet() {
        Assert.assertEquals(SQLHandler.Type.SET, 0xff & SimpleSqlParser.parse("set ..."));
        Assert.assertEquals(SQLHandler.Type.SET, 0xff & SimpleSqlParser.parse("SET ..."));
        Assert.assertEquals(SQLHandler.Type.SET, 0xff & SimpleSqlParser.parse("sEt ..."));
    }

    @Test
    public void testIsShow() {
        Assert.assertEquals(SQLHandler.Type.SHOW, 0xff & SimpleSqlParser.parse("show ..."));
        Assert.assertEquals(SQLHandler.Type.SHOW, 0xff & SimpleSqlParser.parse("SHOW ..."));
        Assert.assertEquals(SQLHandler.Type.SHOW, 0xff & SimpleSqlParser.parse("sHOw ..."));
    }

    @Test
    public void testIsStart() {
        Assert.assertEquals(SQLHandler.Type.START, 0xff & SimpleSqlParser.parse("start ..."));
        Assert.assertEquals(SQLHandler.Type.START, 0xff & SimpleSqlParser.parse("START ..."));
        Assert.assertEquals(SQLHandler.Type.START, 0xff & SimpleSqlParser.parse("stART ..."));
    }

    @Test
    public void testIsUpdate() {
        Assert.assertEquals(SQLHandler.Type.UPDATE, SimpleSqlParser.parse("update ..."));
        Assert.assertEquals(SQLHandler.Type.UPDATE, SimpleSqlParser.parse("UPDATE ..."));
        Assert.assertEquals(SQLHandler.Type.UPDATE, SimpleSqlParser.parse("UPDate ..."));
    }

    @Test
    public void testIsShowDatabases() {
        Assert.assertEquals(ServerParseShow.DATABASES, ServerParseShow.parse("show databases", 4));
        Assert.assertEquals(ServerParseShow.DATABASES, ServerParseShow.parse("SHOW DATABASES", 4));
        Assert.assertEquals(ServerParseShow.DATABASES, ServerParseShow.parse("SHOW databases ", 4));
    }

    @Test
    public void testIsShowDataSources() {
        Assert.assertEquals(ServerParseShow.DATASOURCES, ServerParseShow.parse("show datasources", 4));
        Assert.assertEquals(ServerParseShow.DATASOURCES, ServerParseShow.parse("SHOW DATASOURCES", 4));
        Assert.assertEquals(ServerParseShow.DATASOURCES, ServerParseShow.parse("  SHOW   DATASOURCES  ", 6));
    }

    @Test
    public void testShowMycatStatus() {
        Assert.assertEquals(ServerParseShow.MYCAT_STATUS, ServerParseShow.parse("show mycat_status", 4));
        Assert.assertEquals(ServerParseShow.MYCAT_STATUS, ServerParseShow.parse("show mycat_status ", 4));
        Assert.assertEquals(ServerParseShow.MYCAT_STATUS, ServerParseShow.parse(" SHOW MYCAT_STATUS", " SHOW".length()));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse(" show mycat_statu", " SHOW".length()));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse(" show mycat_status2", " SHOW".length()));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse("Show mycat_status2 ", "SHOW".length()));
    }

    @Test
    public void testShowMycatCluster() {
        Assert.assertEquals(ServerParseShow.MYCAT_CLUSTER, ServerParseShow.parse("show mycat_cluster", 4));
        Assert.assertEquals(ServerParseShow.MYCAT_CLUSTER, ServerParseShow.parse("Show mycat_CLUSTER ", 4));
        Assert.assertEquals(ServerParseShow.MYCAT_CLUSTER, ServerParseShow.parse(" show  MYCAT_cluster", 5));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse(" show mycat_clust", 5));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse(" show mycat_cluster2", 5));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse("Show mycat_cluster9 ", 4));
    }

    @Test
    public void testIsShowOther() {
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse("show ...", 4));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse("SHOW ...", 4));
        Assert.assertEquals(ServerParseShow.OTHER, ServerParseShow.parse("SHOW ... ", 4));
    }

    @Test
    public void testIsSetAutocommitOn() {
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_ON, ServerParseSet.parse("set autocommit=1", 3));
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_ON, ServerParseSet.parse("set autoCOMMIT = 1", 3));
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_ON, ServerParseSet.parse("SET AUTOCOMMIT=on", 3));
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_ON, ServerParseSet.parse("set autoCOMMIT = ON", 3));
    }

    @Test
    public void testIsSetAutocommitOff() {
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_OFF, ServerParseSet.parse("set autocommit=0", 3));
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_OFF, ServerParseSet.parse("SET AUTOCOMMIT= 0", 3));
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_OFF, ServerParseSet.parse("set autoCOMMIT =OFF", 3));
        Assert.assertEquals(ServerParseSet.AUTOCOMMIT_OFF, ServerParseSet.parse("set autoCOMMIT = off", 3));
    }

    @Test
    public void testIsSetNames() {
        Assert.assertEquals(ServerParseSet.NAMES, 0xff & ServerParseSet.parse("set names utf8", 3));
        Assert.assertEquals(ServerParseSet.NAMES, 0xff & ServerParseSet.parse("SET NAMES UTF8", 3));
        Assert.assertEquals(ServerParseSet.NAMES, 0xff & ServerParseSet.parse("set NAMES utf8", 3));
    }

    @Test
    public void testIsCharacterSetResults() {
        Assert.assertEquals(ServerParseSet.CHARACTER_SET_RESULTS,
                0xff & ServerParseSet.parse("SET character_set_results  = NULL", 3));
        Assert.assertEquals(ServerParseSet.CHARACTER_SET_RESULTS,
                0xff & ServerParseSet.parse("SET CHARACTER_SET_RESULTS= NULL", 3));
        Assert.assertEquals(ServerParseSet.CHARACTER_SET_RESULTS,
                0xff & ServerParseSet.parse("Set chARActer_SET_RESults =  NULL", 3));
        Assert.assertEquals(ServerParseSet.CHARACTER_SET_CONNECTION,
                0xff & ServerParseSet.parse("Set chARActer_SET_Connection =  NULL", 3));
        Assert.assertEquals(ServerParseSet.CHARACTER_SET_CLIENT,
                0xff & ServerParseSet.parse("Set chARActer_SET_client =  NULL", 3));
    }

    @Test
    public void testIsSetOther() {
        Assert.assertEquals(ServerParseSet.OTHER, ServerParseSet.parse("set ...", 3));
        Assert.assertEquals(ServerParseSet.OTHER, ServerParseSet.parse("SET ...", 3));
        Assert.assertEquals(ServerParseSet.OTHER, ServerParseSet.parse("sEt ...", 3));
    }

    @Test
    public void testIsKill() {
        Assert.assertEquals(SQLHandler.Type.KILL, 0xff & SimpleSqlParser.parse(" kill  ..."));
        Assert.assertEquals(SQLHandler.Type.KILL, 0xff & SimpleSqlParser.parse("kill 111111 ..."));
        Assert.assertEquals(SQLHandler.Type.KILL, 0xff & SimpleSqlParser.parse("KILL  1335505632"));
    }

    @Test
    public void testIsKillQuery() {
        Assert.assertEquals(SQLHandler.Type.KILL_QUERY, 0xff & SimpleSqlParser.parse(" kill query ..."));
        Assert.assertEquals(SQLHandler.Type.KILL_QUERY, 0xff & SimpleSqlParser.parse("kill   query 111111 ..."));
        Assert.assertEquals(SQLHandler.Type.KILL_QUERY, 0xff & SimpleSqlParser.parse("KILL QUERY 1335505632"));
    }

    @Test
    public void testIsSavepoint() {
        Assert.assertEquals(SQLHandler.Type.SAVEPOINT, SimpleSqlParser.parse(" savepoint  ..."));
        Assert.assertEquals(SQLHandler.Type.SAVEPOINT, SimpleSqlParser.parse("SAVEPOINT "));
        Assert.assertEquals(SQLHandler.Type.SAVEPOINT, SimpleSqlParser.parse(" SAVEpoint   a"));
    }

    @Test
    public void testIsUse() {
        Assert.assertEquals(SQLHandler.Type.USE, 0xff & SimpleSqlParser.parse(" use  ..."));
        Assert.assertEquals(SQLHandler.Type.USE, 0xff & SimpleSqlParser.parse("USE "));
        Assert.assertEquals(SQLHandler.Type.USE, 0xff & SimpleSqlParser.parse(" Use   a"));
    }

    @Test
    public void testIsStartTransaction() {
        Assert.assertEquals(ServerParseStart.TRANSACTION, ServerParseStart.parse(" start transaction  ...", 6));
        Assert.assertEquals(ServerParseStart.TRANSACTION, ServerParseStart.parse("START TRANSACTION", 5));
        Assert.assertEquals(ServerParseStart.TRANSACTION, ServerParseStart.parse(" staRT   TRANSaction  ", 6));
    }

    @Test
    public void testIsSelectVersionComment() {
        Assert.assertEquals(ServerParseSelect.VERSION_COMMENT,
                ServerParseSelect.parse(" select @@version_comment  ", 7));
        Assert.assertEquals(ServerParseSelect.VERSION_COMMENT, ServerParseSelect.parse("SELECT @@VERSION_COMMENT", 6));
        Assert.assertEquals(ServerParseSelect.VERSION_COMMENT,
                ServerParseSelect.parse(" selECT    @@VERSION_comment  ", 7));
    }

    @Test
    public void testIsSelectVersion() {
        Assert.assertEquals(ServerParseSelect.VERSION, ServerParseSelect.parse(" select version ()  ", 7));
        Assert.assertEquals(ServerParseSelect.VERSION, ServerParseSelect.parse("SELECT VERSION(  )", 6));
        Assert.assertEquals(ServerParseSelect.VERSION, ServerParseSelect.parse(" selECT    VERSION()  ", 7));
    }

    @Test
    public void testIsSelectDatabase() {
        Assert.assertEquals(ServerParseSelect.DATABASE, ServerParseSelect.parse(" select database()  ", 7));
        Assert.assertEquals(ServerParseSelect.DATABASE, ServerParseSelect.parse("SELECT DATABASE()", 6));
        Assert.assertEquals(ServerParseSelect.DATABASE, ServerParseSelect.parse(" selECT    DATABASE()  ", 7));
    }

    @Test
    public void testIsSelectUser() {
        Assert.assertEquals(ServerParseSelect.USER, ServerParseSelect.parse(" select user()  ", 7));
        Assert.assertEquals(ServerParseSelect.USER, ServerParseSelect.parse("SELECT USER()", 6));
        Assert.assertEquals(ServerParseSelect.USER, ServerParseSelect.parse(" selECT    USER()  ", 7));
    }

    @Test
    public void testTxReadUncommitted() {
        Assert.assertEquals(ServerParseSet.TX_READ_UNCOMMITTED,
                ServerParseSet.parse("  SET SESSION TRANSACTION ISOLATION LEVEL READ  UNCOMMITTED  ", "  SET".length()));
        Assert.assertEquals(ServerParseSet.TX_READ_UNCOMMITTED,
                ServerParseSet.parse(" set session transaction isolation level read  uncommitted  ", " SET".length()));
        Assert.assertEquals(ServerParseSet.TX_READ_UNCOMMITTED,
                ServerParseSet.parse(" set session transaCTION ISOLATION LEvel read  uncommitteD ", " SET".length()));
    }

    @Test
    public void testTxReadCommitted() {
        Assert.assertEquals(ServerParseSet.TX_READ_COMMITTED,
                ServerParseSet.parse("  SET SESSION TRANSACTION ISOLATION LEVEL READ  COMMITTED  ", "  SET".length()));
        Assert.assertEquals(ServerParseSet.TX_READ_COMMITTED,
                ServerParseSet.parse(" set session transaction isolation level read  committed  ", " SET".length()));
        Assert.assertEquals(ServerParseSet.TX_READ_COMMITTED,
                ServerParseSet.parse(" set session transaCTION ISOLATION LEVel read  committed ", " SET".length()));
    }

    @Test
    public void testTxRepeatedRead() {
        Assert.assertEquals(ServerParseSet.TX_REPEATED_READ,
                ServerParseSet.parse("  SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE   READ  ", "  SET".length()));
        Assert.assertEquals(ServerParseSet.TX_REPEATED_READ,
                ServerParseSet.parse(" set session transaction isolation level repeatable   read  ", " SET".length()));
        Assert.assertEquals(ServerParseSet.TX_REPEATED_READ,
                ServerParseSet.parse(" set session transaction isOLATION LEVEL REPEatable   read ", " SET".length()));
    }

    @Test
    public void testTxSerializable() {
        Assert.assertEquals(ServerParseSet.TX_SERIALIZABLE,
                ServerParseSet.parse("  SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE  ", "  SET".length()));
        Assert.assertEquals(ServerParseSet.TX_SERIALIZABLE,
                ServerParseSet.parse(" set session transaction   isolation level serializable  ", " SET".length()));
        Assert.assertEquals(ServerParseSet.TX_SERIALIZABLE,
                ServerParseSet.parse(" set session   transaction  isOLATION LEVEL SERIAlizable ", " SET".length()));
    }

    @Test
    public void testIdentity() {
        String stmt = "select @@identity";
        int indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterIdentity(stmt, stmt.indexOf('i'));
        Assert.assertEquals(stmt.length(), indexAfterLastInsertIdFunc);
        Assert.assertEquals(ServerParseSelect.IDENTITY, ServerParseSelect.parse(stmt, 6));
        stmt = "select  @@identity as id";
        Assert.assertEquals(ServerParseSelect.IDENTITY, ServerParseSelect.parse(stmt, 6));
        stmt = "select  @@identitY  id";
        Assert.assertEquals(ServerParseSelect.IDENTITY, ServerParseSelect.parse(stmt, 6));
        stmt = "select  /*foo*/@@identitY  id";
        Assert.assertEquals(ServerParseSelect.IDENTITY, ServerParseSelect.parse(stmt, 6));
        stmt = "select/*foo*/ @@identitY  id";
        Assert.assertEquals(ServerParseSelect.IDENTITY, ServerParseSelect.parse(stmt, 6));
        stmt = "select/*foo*/ @@identitY As id";
        Assert.assertEquals(ServerParseSelect.IDENTITY, ServerParseSelect.parse(stmt, 6));

        stmt = "select  @@identity ,";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select  @@identity as, ";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select  @@identity as id  , ";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select  @@identity ass id   ";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));

    }

    @Test
    public void testLastInsertId() {
        String stmt = " last_insert_iD()";
        int indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.length(), indexAfterLastInsertIdFunc);
        stmt = " last_insert_iD ()";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.length(), indexAfterLastInsertIdFunc);
        stmt = " last_insert_iD ( /**/ )";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.length(), indexAfterLastInsertIdFunc);
        stmt = " last_insert_iD (  )  ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.lastIndexOf(')') + 1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id(  )";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.lastIndexOf(')') + 1, indexAfterLastInsertIdFunc);
        stmt = "last_iNsert_id(  ) ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.lastIndexOf(')') + 1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_iD";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(-1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_i     ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(-1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_i    d ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(-1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id (     ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(-1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id(  d)     ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(-1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id(  ) d    ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.lastIndexOf(')') + 1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id(d)";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(-1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id(#\r\nd) ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(-1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id(#\n\r) ";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.lastIndexOf(')') + 1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id (#\n\r)";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.lastIndexOf(')') + 1, indexAfterLastInsertIdFunc);
        stmt = " last_insert_id(#\n\r)";
        indexAfterLastInsertIdFunc = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, stmt.indexOf('l'));
        Assert.assertEquals(stmt.lastIndexOf(')') + 1, indexAfterLastInsertIdFunc);

        stmt = "select last_insert_id(#\n\r)";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) as id";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) as `id`";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) as 'id'";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)  id";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)  `id`";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)  'id'";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) a";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        // NOTE: this should be invalid, ignore this bug
        stmt = "select last_insert_id(#\n\r) as";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) asd";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        // NOTE: this should be invalid, ignore this bug
        stmt = "select last_insert_id(#\n\r) as 777";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        // NOTE: this should be invalid, ignore this bug
        stmt = "select last_insert_id(#\n\r)  777";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)as `77``7`";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)ass";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)as 'a'";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)as 'a\\''";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)as 'a'''";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)as 'a\"'";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 6));
        stmt = "   select last_insert_id(#\n\r) As 'a\"'";
        Assert.assertEquals(ServerParseSelect.LAST_INSERT_ID, ServerParseSelect.parse(stmt, 9));

        stmt = "select last_insert_id(#\n\r)as 'a\"\\'";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)as `77``7` ,";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r)as `77`7`";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) as,";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) ass a";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
        stmt = "select last_insert_id(#\n\r) as 'a";
        Assert.assertEquals(ServerParseSelect.OTHER, ServerParseSelect.parse(stmt, 6));
    }
    
    @Test
    public void testLockTable() {
    	Assert.assertEquals(SQLHandler.Type.LOCK, SimpleSqlParser.parse("lock tables ttt write;"));
    	Assert.assertEquals(SQLHandler.Type.LOCK, SimpleSqlParser.parse(" lock tables ttt read;"));
    	Assert.assertEquals(SQLHandler.Type.LOCK, SimpleSqlParser.parse("lock tables"));
    }

    @Test
    public void testUnlockTable() {
    	Assert.assertEquals(SQLHandler.Type.UNLOCK, SimpleSqlParser.parse("unlock tables"));
    	Assert.assertEquals(SQLHandler.Type.UNLOCK, SimpleSqlParser.parse(" unlock	 tables"));
    }
    
    @Test
    public void testSetXAOn() {
    	Assert.assertEquals(ServerParseSet.XA_FLAG_ON, ServerParseSet.parse("set xa=on", 3));
    	Assert.assertEquals(ServerParseSet.XA_FLAG_ON, ServerParseSet.parse("set xa = on", 3));
    	Assert.assertEquals(ServerParseSet.XA_FLAG_ON, ServerParseSet.parse("set xa \t\n\r = \t\n\r on", 3));
    }
    
    @Test
    public void testSetXAOff() {
    	Assert.assertEquals(ServerParseSet.XA_FLAG_OFF, ServerParseSet.parse("set xa=off", 3));
    	Assert.assertEquals(ServerParseSet.XA_FLAG_OFF, ServerParseSet.parse("set xa = off", 3));
    	Assert.assertEquals(ServerParseSet.XA_FLAG_OFF, ServerParseSet.parse("set xa \t\n\r = \t\n\r off", 3));
    }

}