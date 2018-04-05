package io.mycat.ext;

import io.mycat.MycatServer;

/**
 * Mycat 扩展模块
 * @author jeff.cao
 * @version 0.0.1, 2018年4月2日 上午11:22:40 
 */
public interface MycatMoudle {

    /**
     * 启动服务
     * 
     * @return
     */
    boolean start(MycatServer server);

    /***
     * 停止服务
     * 
     * @return
     */
    boolean stop(MycatServer server);
}
