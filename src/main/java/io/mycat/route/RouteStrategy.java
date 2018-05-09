package io.mycat.route;

import java.sql.SQLNonTransientException;

import io.mycat.cache.LayerCachePool;
import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.net.plus.ClientConn;

/**
 * 路由策略接口
 * @author wang.dw
 *
 */
public interface RouteStrategy {
    
    public RouteResultset route(SystemConfig sysConfig, SchemaConfig schema, int sqlType, String origSQL,
                                String charset, ClientConn sc,
                                LayerCachePool cachePool) throws SQLNonTransientException;
}
