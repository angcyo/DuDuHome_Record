package com.dudu.network.client.netty;


import com.dudu.network.client.IConnectCallBack;
import com.dudu.network.client.IConnection;
import com.dudu.network.valueobject.ConnectionParam;
import com.dudu.network.valueobject.ConnectionState;
import com.dudu.obd.common.BaseMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


/**
 * Created by dengjun on 2016/3/3.
 * Description :
 */
@ChannelHandler.Sharable
public class NettyConnection extends SimpleChannelInboundHandler<BaseMsg> implements IConnection <BaseMsg>  {
    private boolean isConnected = false;
    private IConnectCallBack iConnectCallBack;

    private SocketChannel socketChannel;
    private EventLoopGroup eventLoopGroup;
//    Bootstrap bootstrap;

    private Logger log;
    public NettyConnection() {
        log = LoggerFactory.getLogger("network");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, BaseMsg msg) throws Exception {
//        log.debug("收到消息------");
        if (iConnectCallBack != null){
            iConnectCallBack.onReceive(msg);
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void connect(ConnectionParam connectionParam) {
        doConnect(connectionParam);
    }

    @Override
    public void disConnect() {
        log.debug("断开连接");
        isConnected =false;
        if(/*isConnected == true && */socketChannel != null){
            try {
//                log.info("关闭socketChannel");
//                socketChannel.closeFuture().sync();
            } catch (Exception e) {
                log.error("异常",e);
            }finally {
//                log.info("关闭eventLoopGroup");
                eventLoopGroup.shutdownGracefully();
                socketChannel = null;
            }
        }
    }

    @Override
    public void setConnectCallBack(IConnectCallBack iConnectCallBack) {
        this.iConnectCallBack = iConnectCallBack;
    }

    @Override
    public void sendMessage(BaseMsg sendMessage) {
        if (socketChannel != null && isConnected == true){
//            log.debug("netty 发送消息 messageID = {}", sendMessage.getMessageId());
            socketChannel.writeAndFlush(sendMessage);
        }
    }

    private void doConnect(ConnectionParam connectionParam){
        try {
            log.debug("连接IP：{}，端口：{}", connectionParam.getHost(), connectionParam.getPort());

            /*EventLoopGroup */eventLoopGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.remoteAddress(connectionParam.getHost(), connectionParam.getPort());

            bootstrap.handler(new ChannelInitializer<SocketChannel>(){

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    log.debug("initChannel--------");
                    //socketChannel.pipeline().addLast(new IdleStateHandler(20, 200, 0));
                    socketChannel.pipeline().addLast(new ObjectEncoder());
                    socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                    socketChannel.pipeline().addLast(NettyConnection.this);
                }
            });
            ChannelFuture future = bootstrap.connect(connectionParam.getHost(), connectionParam.getPort()).sync();

            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                log.debug("connect server  成功---------");
                isConnected = true;
                if (iConnectCallBack != null){
                    iConnectCallBack.onConnectionState(new ConnectionState(ConnectionState.CONNECTION_SUCCESS));
                }
            }else {
                log.debug("connect server  失败---------");
                isConnected =false;
                if (iConnectCallBack != null){
                    iConnectCallBack.onConnectionState(new ConnectionState(ConnectionState.CONNECTION_FAIL));
                }
            }
        } catch (InterruptedException e) {
            log.error("异常",e);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
        isConnected = false;
        log.error("连接异常");
        if (iConnectCallBack != null){
            iConnectCallBack.onConnectionState(new ConnectionState(ConnectionState.CONNECTION_FAIL));
        }
    }
}
