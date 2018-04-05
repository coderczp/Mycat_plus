package io.mycat.ext.mq;

import java.util.EventObject;

import io.mycat.ext.MycatMoudle;

/**
 * 把MyCat内部的事件通过MQ publish,第三方可以订阅
 * @author jeff.cao
 * @version 0.0.1, 2018年4月2日 上午11:16:35 
 */
public interface MQServer extends MycatMoudle {

    /***
     * 发布MQ消息
     * 
     * @param event
     * @return
     */
    boolean publish(EventObject event);
}
