package io.mycat.cache.impl;

import java.io.File;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import io.mycat.cache.CachePool;
import io.mycat.cache.CachePoolFactory;

public class LevelDBCachePooFactory extends CachePoolFactory {

    @Override
    public CachePool createCachePool(String poolName, int cacheSize, int expireSeconds) {
        try {
            File path = new File("leveldb", poolName);
            Options options = new Options();
            options.createIfMissing(true);
            options.cacheSize(cacheSize * 1048576);
            DB db = Iq80DBFactory.factory.open(path, options);
            return new LevelDBPool(poolName, db, cacheSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
