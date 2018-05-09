///*
// * Copyright (c) 2018, MyCat_Plus and/or its affiliates. All rights reserved.
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
// *
// * This code is free software;Designed and Developed mainly by many Chinese 
// * opensource volunteers. you can redistribute it and/or modify it under the 
// * terms of the GNU General Public License version 2 only, as published by the
// * Free Software Foundation.
// *
// * This code is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// * version 2 for more details (a copy is included in the LICENSE file that
// * accompanied this code).
// *
// * You should have received a copy of the GNU General Public License version
// * 2 along with this work; if not, write to the Free Software Foundation,
// * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
// * 
// * Any questions about this component can be directed to it's project Web address 
// * https://code.google.com/p/opencloudb/.
// *
// */
//
//package io.mycat.plus.net.impl;
//
//import java.net.InetSocketAddress;
//import java.nio.channels.SocketChannel;
//import java.util.concurrent.TimeUnit;
//
//import org.springframework.core.codec.StringDecoder;
//
//import io.mycat.MycatServer;
//import io.mycat.plus.ProtocolHandler;
//import io.mycat.plus.net.ITransport;
//
///**
// * @author jeff.cao
// * @version 0.0.1, 2018年4月28日 下午11:53:41 
// */
//public class NettyTransportImpl implements ITransport {
//
//    @Override
//    public void start() throws Exception {
//        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        try {
//            ServerBootstrap sbs = new ServerBootstrap().group(bossGroup, workerGroup)
//                .channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
//                .localAddress(new InetSocketAddress(port)).childHandler(new ChannelInitializer<SocketChannel>() {
//                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
//                        ch.pipeline().addLast("decoder", new StringDecoder());
//                        ch.pipeline().addLast("encoder", new StringEncoder());
//                        ch.pipeline().addLast(new HeartBeatServerHandler());
//                    };
//
//                }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
//            // 绑定端口，开始接收进来的连接  
//            ChannelFuture future = sbs.bind(port).sync();
//
//            System.out.println("Server start listen at " + port);
//            future.channel().closeFuture().sync();
//        } catch (Exception e) {
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//        }
//    }
//
//    @Override
//    public void stop() throws Exception {
//    }
//
//    @Override
//    public void setContext(MycatServer context) {
//    }
//
//    @Override
//    public void setProtocolHandler(ProtocolHandler handler) {
//    }
//
//}
