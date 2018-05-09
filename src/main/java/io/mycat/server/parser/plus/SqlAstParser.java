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

package io.mycat.server.parser.plus;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLBlockStatement;
import com.alibaba.druid.sql.ast.statement.SQLCallStatement;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLGrantStatement;
import com.alibaba.druid.sql.ast.statement.SQLMergeStatement;
import com.alibaba.druid.sql.ast.statement.SQLRollbackStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLStartTransactionStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExecuteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExplainStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHelpStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHintStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLockTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLockTableStatement.LockType;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplaceStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnlockTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MysqlDeallocatePrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;

import io.mycat.server.handler.plus.SQLHandler.Type;

/**
 * @author jeff.cao
 * @version 0.0.1, 2018年4月27日 下午7:47:52 
 */
public class SqlAstParser extends MySqlStatementParser {

    public SqlAstParser(String sql, boolean keepComments) {
        super(sql, keepComments);
    }

    public List<StatementHolder> parseStatements() {
        List<StatementHolder> statementList = new ArrayList<StatementHolder>();
        for (;;) {
            if (lexer.token() == Token.EOF || lexer.token() == Token.END) {
                int size = statementList.size();
                if (lexer.isKeepComments() && lexer.hasComment() && size > 0) {
                    SQLStatement stmt = statementList.get(statementList.size() - 1).getStmt();
                    stmt.addAfterComment(lexer.readAndResetComments());
                }
                return statementList;
            }

            if (lexer.token() == Token.SEMI) {
                int line0 = lexer.getLine();
                lexer.nextToken();
                int line1 = lexer.getLine();

                if (lexer.isKeepComments() && statementList.size() > 0) {
                    SQLStatement stmt = statementList.get(statementList.size() - 1).getStmt();
                    if (line1 - line0 <= 1) {
                        stmt.addAfterComment(lexer.readAndResetComments());
                    }
                    stmt.getAttributes().put("format.semi", Boolean.TRUE);
                }
                continue;
            }

            if (lexer.token() == Token.SELECT) {
                SQLStatement select = parseSelect();
                statementList.add(new StatementHolder(Type.SELECT, select));
                continue;
            }

            if (lexer.token() == (Token.UPDATE)) {
                SQLUpdateStatement update = parseUpdateStatement();
                statementList.add(new StatementHolder(Type.UPDATE, update));
                continue;
            }

            if (lexer.token() == (Token.CREATE)) {
                SQLStatement create = parseCreate();
                statementList.add(new StatementHolder(Type.CREATE, create));
                continue;
            }

            if (lexer.token() == (Token.INSERT)) {
                SQLStatement insert = parseInsert();
                statementList.add(new StatementHolder(Type.INSERT, insert));
                continue;
            }

            if (lexer.token() == (Token.DELETE)) {
                MySqlDeleteStatement delete = parseDeleteStatement();
                statementList.add(new StatementHolder(Type.DELETE, delete));
                continue;
            }

            if (lexer.token() == (Token.EXPLAIN)) {
                MySqlExplainStatement explain = parseExplain();
                statementList.add(new StatementHolder(Type.EXPLAIN, explain));
                continue;
            }

            if (lexer.token() == Token.SET) {
                SQLStatement set = parseSet();
                statementList.add(new StatementHolder(Type.SET, set));
                continue;
            }

            if (lexer.token() == Token.ALTER) {
                SQLStatement alter = parseAlter();
                statementList.add(new StatementHolder(Type.ALTER, alter));
                continue;
            }

            if (lexer.token() == Token.DROP) {
                List<String> beforeComments = null;
                if (lexer.isKeepComments() && lexer.hasComment()) {
                    beforeComments = lexer.readAndResetComments();
                }

                lexer.nextToken();

                if (lexer.token() == Token.TABLE || identifierEquals("TEMPORARY")) {

                    SQLDropTableStatement stmt = parseDropTable(false);

                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }

                    statementList.add(new StatementHolder(Type.DROP_TABLE, stmt));
                    continue;
                } else if (lexer.token() == Token.USER) {
                    SQLStatement stmt = parseDropUser();
                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }
                    statementList.add(new StatementHolder(Type.DROP_USER, stmt));
                    continue;
                } else if (lexer.token() == Token.INDEX) {
                    SQLStatement stmt = parseDropIndex();
                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }
                    statementList.add(new StatementHolder(Type.DROP_INDEX, stmt));
                    continue;
                } else if (lexer.token() == Token.VIEW) {
                    SQLStatement stmt = parseDropView(false);

                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }

                    statementList.add(new StatementHolder(Type.DROP_VIEW, stmt));
                    continue;
                } else if (lexer.token() == Token.TRIGGER) {
                    SQLStatement stmt = parseDropTrigger(false);

                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }

                    statementList.add(new StatementHolder(Type.DROP_TRIGGER, stmt));
                    continue;
                } else if (lexer.token() == Token.DATABASE) {
                    SQLStatement stmt = parseDropDatabase(false);

                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }

                    statementList.add(new StatementHolder(Type.DROP_DB, stmt));
                    continue;
                } else if (lexer.token() == Token.FUNCTION) {
                    SQLStatement stmt = parseDropFunction(false);

                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }

                    statementList.add(new StatementHolder(Type.DROP_FUNCTION, stmt));
                    continue;
                } else if (lexer.token() == Token.TABLESPACE) {
                    SQLStatement stmt = parseDropTablespace(false);

                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }

                    statementList.add(new StatementHolder(Type.DROP_TABLESPACE, stmt));
                    continue;
                } else if (lexer.token() == Token.PROCEDURE) {
                    SQLStatement stmt = parseDropProcedure(false);

                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }

                    statementList.add(new StatementHolder(Type.DROP_PROCEDURE, stmt));
                    continue;
                } else if (lexer.token() == Token.SEQUENCE) {
                    SQLStatement stmt = parseDropSequece(false);
                    if (beforeComments != null) {
                        stmt.addBeforeComment(beforeComments);
                    }
                    statementList.add(new StatementHolder(Type.DROP_SEQUENCE, stmt));
                    continue;
                } else {
                    throw new ParserException("TODO " + lexer.token());
                }
            }

            if (lexer.token() == Token.TRUNCATE) {
                SQLStatement stmt = parseTruncate();
                statementList.add(new StatementHolder(Type.TRUNCATE, stmt));
                continue;
            }

            if (lexer.token() == Token.USE) {
                SQLUseStatement stmt = parseUse();
                statementList.add(new StatementHolder(Type.USE, stmt));
                continue;
            }

            if (lexer.token() == Token.GRANT) {
                SQLGrantStatement stmt = parseGrant();
                statementList.add(new StatementHolder(Type.GRANT, stmt));
                continue;
            }

            if (lexer.token() == Token.REVOKE) {
                SQLStatement stmt = parseRevoke();
                statementList.add(new StatementHolder(Type.REVOKE, stmt));
                continue;
            }

            if (lexer.token() == Token.LBRACE || identifierEquals("CALL")) {
                SQLCallStatement stmt = parseCall();
                statementList.add(new StatementHolder(Type.CALL, stmt));
                continue;
            }

            if (identifierEquals("RENAME")) {
                SQLStatement stmt = parseRename();
                statementList.add(new StatementHolder(Type.RENAME, stmt));
                continue;
            }

            if (identifierEquals("RELEASE")) {
                SQLStatement stmt = parseReleaseSavePoint();
                statementList.add(new StatementHolder(Type.RELEASE, stmt));
                continue;
            }

            if (identifierEquals("SAVEPOINT")) {
                SQLStatement stmt = parseSavePoint();
                statementList.add(new StatementHolder(Type.SAVEPOINT, stmt));
                continue;
            }

            if (identifierEquals("ROLLBACK")) {
                SQLRollbackStatement stmt = parseRollback();
                statementList.add(new StatementHolder(Type.ROLLBACK, stmt));
                continue;
            }

            if (identifierEquals("COMMIT")) {
                SQLStatement stmt = parseCommit();
                statementList.add(new StatementHolder(Type.COMMIT, stmt));
                continue;
            }

            if (lexer.token() == Token.SHOW) {
                SQLStatement stmt = parseShow();
                statementList.add(new StatementHolder(Type.SHOW, stmt));
                continue;
            }

            if (lexer.token() == Token.LPAREN) {
                char markChar = lexer.current();
                int markBp = lexer.bp();
                lexer.nextToken();
                if (lexer.token() == Token.SELECT) {
                    lexer.reset(markBp, markChar, Token.LPAREN);
                    SQLStatement stmt = parseSelect();
                    statementList.add(new StatementHolder(Type.SUB_SELECT, stmt));
                    continue;
                }
            }

            if (lexer.token() == Token.MERGE) {
                SQLMergeStatement stmt = parseMerge();
                statementList.add(new StatementHolder(Type.MERGE, stmt));
                continue;
            }

            if (parseStatementDialect(statementList)) {
                continue;
            }

            if (lexer.token() == Token.COMMENT) {
                SQLCommentStatement stmt = this.parseComment();
                statementList.add(new StatementHolder(Type.MYSQL_COMMENT, stmt));
                continue;
            }

            if (lexer.token() == Token.UPSERT || identifierEquals("UPSERT")) {
                SQLStatement stmt = parseUpsert();
                statementList.add(new StatementHolder(Type.UPSERT, stmt));
                continue;
            }

            printError(lexer.token());
        }
    }

    private boolean parseStatementDialect(List<StatementHolder> statementList) {
        if (lexer.token() == Token.KILL) {
            SQLStatement stmt = parseKill();
            statementList.add(new StatementHolder(Type.KILL, stmt));
            return true;
        }

        if (identifierEquals("PREPARE")) {
            MySqlPrepareStatement stmt = parsePrepare();
            statementList.add(new StatementHolder(Type.PREPARE, stmt));
            return true;
        }

        if (identifierEquals("EXECUTE")) {
            MySqlExecuteStatement stmt = parseExecute();
            statementList.add(new StatementHolder(Type.EXECUTE, stmt));
            return true;
        }

        if (identifierEquals("DEALLOCATE")) {
            MysqlDeallocatePrepareStatement stmt = parseDeallocatePrepare();
            statementList.add(new StatementHolder(Type.DEALLOCATE, stmt));
            return true;
        }

        if (identifierEquals("LOAD")) {
            SQLStatement stmt = parseLoad();
            statementList.add(new StatementHolder(Type.LOAD_DATA_INFILE_SQL, stmt));
            return true;
        }

        if (lexer.token() == Token.REPLACE) {
            MySqlReplaceStatement stmt = parseReplicate();
            statementList.add(new StatementHolder(Type.REPLACE, stmt));
            return true;
        }

        if (identifierEquals("START")) {
            SQLStartTransactionStatement stmt = parseStart();
            statementList.add(new StatementHolder(Type.START, stmt));
            return true;
        }

        if (lexer.token() == Token.SHOW) {
            SQLStatement stmt = parseShow();
            statementList.add(new StatementHolder(Type.SHOW, stmt));
            return true;
        }

        if (lexer.token() == Token.EXPLAIN) {
            SQLStatement stmt = this.parseExplain();
            statementList.add(new StatementHolder(Type.EXPLAIN, stmt));
            return true;
        }

        if (identifierEquals("BINLOG")) {
            SQLStatement stmt = parseBinlog();
            statementList.add(new StatementHolder(Type.BINLOG, stmt));
            return true;
        }

        if (identifierEquals("RESET")) {
            SQLStatement stmt = parseReset();
            statementList.add(new StatementHolder(Type.RESET, stmt));
            return true;
        }

        if (lexer.token() == Token.ANALYZE) {
            SQLStatement stmt = parseAnalyze();
            statementList.add(new StatementHolder(Type.ANALYZE, stmt));
            return true;
        }

        if (lexer.token() == Token.OPTIMIZE) {
            SQLStatement stmt = parseOptimize();
            statementList.add(new StatementHolder(Type.OPTIMIZE, stmt));
            return true;
        }

        if (identifierEquals("HELP")) {
            lexer.nextToken();
            MySqlHelpStatement stmt = new MySqlHelpStatement();
            stmt.setContent(this.exprParser.primary());
            statementList.add(new StatementHolder(Type.HELP, stmt));
            return true;
        }

        if (lexer.token() == Token.DESC || identifierEquals("DESCRIBE")) {
            SQLStatement stmt = parseDescribe();
            statementList.add(new StatementHolder(Type.DESCRIBE, stmt));
            return true;
        }

        if (lexer.token() == Token.LOCK) {
            lexer.nextToken();
            String val = lexer.stringVal();
            boolean isLockTables = "TABLES".equalsIgnoreCase(val) && lexer.token() == Token.IDENTIFIER;
            boolean isLockTable = "TABLE".equalsIgnoreCase(val) && lexer.token() == Token.TABLE;
            if (isLockTables || isLockTable) {
                lexer.nextToken();
            } else {
                setErrorEndPos(lexer.pos());
                throw new ParserException("syntax error, expect TABLES or TABLE, actual " + lexer.token());
            }

            MySqlLockTableStatement stmt = new MySqlLockTableStatement();
            stmt.setTableSource(this.exprParser.name());

            if (identifierEquals("READ")) {
                lexer.nextToken();
                if (identifierEquals("LOCAL")) {
                    lexer.nextToken();
                    stmt.setLockType(LockType.READ_LOCAL);
                } else {
                    stmt.setLockType(LockType.READ);
                }
            } else if (identifierEquals("WRITE")) {
                stmt.setLockType(LockType.WRITE);
            } else if (identifierEquals("LOW_PRIORITY")) {
                lexer.nextToken();
                acceptIdentifier("WRITE");
                stmt.setLockType(LockType.LOW_PRIORITY_WRITE);
            } else {
                throw new ParserException("syntax error, expect READ or WRITE, actual " + lexer.token());
            }

            if (lexer.token() == Token.HINT) {
                stmt.setHints(this.exprParser.parseHints());
            }
            statementList.add(new StatementHolder(Type.LOCK, stmt));
            return true;
        }

        if (identifierEquals("UNLOCK")) {
            lexer.nextToken();
            String val = lexer.stringVal();
            boolean isUnLockTables = "TABLE".equalsIgnoreCase(val) && lexer.token() == Token.IDENTIFIER;
            boolean isUnLockTable = "TABLE".equalsIgnoreCase(val) && lexer.token() == Token.TABLE;
            MySqlUnlockTablesStatement stmt = new MySqlUnlockTablesStatement();
            statementList.add(new StatementHolder(Type.UNLOCK, stmt));
            if (isUnLockTables || isUnLockTable) {
                lexer.nextToken();
            } else {
                setErrorEndPos(lexer.pos());
                throw new ParserException("syntax error, expect TABLES or TABLE, actual " + lexer.token());
            }
            return true;
        }

        if (lexer.token() == Token.HINT) {
            List<SQLCommentHint> hints = this.exprParser.parseHints();

            boolean tddlSelectHints = false;

            if (hints.size() == 1 && statementList.size() == 0 && lexer.token() == Token.SELECT) {
                SQLCommentHint hint = hints.get(0);
                String hintText = hint.getText();
                if (hintText.startsWith("+TDDL")) {
                    tddlSelectHints = true;
                }
            }

            if (tddlSelectHints) {
                SQLSelectStatement stmt = (SQLSelectStatement) this.parseStatement();
                stmt.setHeadHints(hints);
                statementList.add(new StatementHolder(Type.SELECT_WITH_HINT, stmt));
                return true;
            }

            MySqlHintStatement stmt = new MySqlHintStatement();
            stmt.setHints(hints);
            statementList.add(new StatementHolder(Type.HINT, stmt));
            return true;
        }

        if (lexer.token() == Token.BEGIN) {
            SQLBlockStatement stmt = this.parseBlock();
            statementList.add(new StatementHolder(Type.BEGIN, stmt));
            return true;
        }

        return false;
    }

}
