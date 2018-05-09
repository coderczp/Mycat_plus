package io.mycat.route;

import io.mycat.config.model.SchemaConfig;
import io.mycat.server.NonBlockingSession;
import io.mycat.server.Session;

public class SessionSQLPair {
    public final SchemaConfig schema;
	public final Session session;
	public final String sql;
	public final int type;

	public SessionSQLPair(Session session, SchemaConfig schema,
			String sql,int type) {
		super();
		this.session = session;
		this.schema = schema;
		this.sql = sql;
		this.type=type;
	}

}
