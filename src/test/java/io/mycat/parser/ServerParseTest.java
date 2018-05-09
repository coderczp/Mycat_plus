package io.mycat.parser;

import junit.framework.Assert;

import org.junit.Test;

import io.mycat.server.handler.plus.SQLHandler;
import io.mycat.server.parser.SimpleSqlParser;

public class ServerParseTest {
	/**
	 * public static final int OTHER = -1;
	public static final int BEGIN = 1;
	public static final int COMMIT = 2;
	public static final int DELETE = 3;
	public static final int INSERT = 4;
	public static final int REPLACE = 5;
	public static final int ROLLBACK = 6;
	public static final int SELECT = 7;
	public static final int SET = 8;
	public static final int SHOW = 9;
	public static final int START = 10;
	public static final int UPDATE = 11;
	public static final int KILL = 12;
	public static final int SAVEPOINT = 13;
	public static final int USE = 14;
	public static final int EXPLAIN = 15;
	public static final int KILL_QUERY = 16;
	public static final int HELP = 17;
	public static final int MYSQL_CMD_COMMENT = 18;
	public static final int MYSQL_COMMENT = 19;
	public static final int CALL = 20;
	public static final int DESCRIBE = 21;
	 */

	@Test
	public void testDesc() {
		String sql = "desc a";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.DESCRIBE, sqlType);
	}
	
	@Test
	public void testDescribe() {
		String sql = "describe a";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.DESCRIBE, sqlType);
	}
	
	@Test
	public void testDelete() {
		String sql = "delete from a where id = 1";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.DELETE, sqlType);
	}
	
	@Test
	public void testInsert() {
		String sql = "insert into a(name) values ('zhangsan')";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.INSERT, sqlType);
	}
	
	@Test
	public void testReplace() {
		String sql = "replace into t(id, update_time) select 1, now();  ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.REPLACE, sqlType);
	}
	
	@Test
	public void testSet() {
		String sql = "SET @var_name = 'value';  ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.SET, sqlType);
	}
	
	@Test
	public void testShow() {
		String sql = "show full tables";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.SHOW, sqlType);
	}
	
	@Test
	public void testStart() {
		String sql = "start ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.START, sqlType);
	}
	
	@Test
	public void testUpdate() {
		String sql = "update a set name='wdw' where id = 1";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.UPDATE, sqlType);
	}
	
	@Test
	public void testKill() {
		String sql = "kill 1";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.KILL, sqlType);
	}
	
	@Test
	public void testSavePoint() {
		String sql = "SAVEPOINT ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.SAVEPOINT, sqlType);
	}
	
	@Test
	public void testUse() {
		String sql = "use db1 ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.USE, sqlType);
	}
	
	@Test
	public void testExplain() {
		String sql = "explain select * from a ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.EXPLAIN, sqlType);
	}
	
	@Test
	public void testKillQuery() {
		String sql = "kill query 1102 ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.KILL_QUERY, sqlType);
	}
	
	@Test
	public void testHelp() {
		String sql = "help contents ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.HELP, sqlType);
	}
	
	@Test
	public void testMysqlCmdComment() {
		
	}
	
	@Test
	public void testMysqlComment() {
		
	}
	
	@Test
	public void testCall() {
		String sql = "CALL demo_in_parameter(@p_in); ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.CALL, sqlType);
	}
	
	@Test
	public void testRollback() {
		String sql = "rollback; ";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.ROLLBACK, sqlType);
	}
	
	@Test
	public void testSelect() {
		String sql = "select * from a";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.SELECT, sqlType);
	}
	
	@Test
	public void testBegin() {
		String sql = "begin";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.BEGIN, sqlType);
	}
	
	@Test
	public void testCommit() {
		String sql = "COMMIT 'nihao'";
		int result = SimpleSqlParser.parse(sql);
		int sqlType = result & 0xff;
		Assert.assertEquals(SQLHandler.Type.COMMIT, sqlType);
	}

}
