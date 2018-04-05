package io.mycat.ext.mq;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.alibaba.fastjson.JSONObject;

import io.mycat.MycatServer;

/**
 * 基于JMQ的server
 * @author jeff.cao
 * @version 0.0.1, 2018年4月2日 上午11:33:38 
 */
public class JeroMQServer implements MQServer {

    private static final Logger LOG = LoggerFactory.getLogger(JeroMQServer.class);

    /***
     * MQ topic
     */
    private String              topic;

    /***
     * zmq上下文
     */
    private Context             zmqCtx;

    /***
     * zmq 发布服务
     */
    private Socket              publisher;

    /** 
     * @see io.mycat.ext.MycatMoudle#start()
     */
    @Override
    public boolean start(MycatServer server) {

        int ioThreads = 2;
        String bind = "tcp://*:5563";

        topic = "CAT";
        zmqCtx = ZMQ.context(ioThreads);
        publisher = zmqCtx.socket(ZMQ.PUB);
        publisher.bind(bind);

        server.getSqlInterceptor();
        LOG.info("mq server started, iothreads:{},address:{}", ioThreads, bind);
        return true;
    }

    /** 
     * @see io.mycat.ext.MycatMoudle#stop()
     */
    @Override
    public boolean stop(MycatServer server) {
        publisher.close();
        zmqCtx.term();
        return false;
    }

    /** 
     * @see io.mycat.ext.mq.MQServer#publish(java.util.EventObject)
     */
    @Override
    public boolean publish(EventObject event) {
        publisher.sendMore(topic);
        String messge = JSONObject.toJSONString(event.getSource());
        boolean send = publisher.send(messge);
        LOG.debug("mq publish {} {}", messge, send);
        return send;
    }

}
