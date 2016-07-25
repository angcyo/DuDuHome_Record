package com.dudu.network.client.netty.server;


import com.dudu.obd.common.AskMsg;
import com.dudu.obd.common.BaseMsg;
import com.dudu.obd.common.BusinessCode;
import com.dudu.obd.common.LoginMsg;
import com.dudu.obd.common.MsgType;
import com.dudu.obd.common.PingMsg;
import com.dudu.obd.common.ReplyClientBody;
import com.dudu.obd.common.ReplyClientLoginBody;
import com.dudu.obd.common.ReplyMsg;
import com.dudu.obd.common.ReplyServerBody;
import com.dudu.obd.common.ReturnCode;
import com.dudu.obd.utils.Encrypt;
import com.dudu.obd.utils.PropertiesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * ObdServerHandler
 * 各种消息的处理类
 * @author    Bob
 * @date      2015年12月3日 下午3:32:09
 */
public class ObdServerHandler extends SimpleChannelInboundHandler<BaseMsg> {
    private Logger log;

    public ObdServerHandler() {
            log = LoggerFactory.getLogger("network");
    }


    /**
     * 当连接断掉的时候需要操作的业务
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //清除当前的SocketChannel
        SocketChannel socketChannel = (SocketChannel) ctx.channel();

        ObdChannelMap.remove((SocketChannel) ctx.channel());
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
        System.out.println("--------messageReceived---------");
        log.debug("服务端--------messageReceived---------");

        SocketChannel socketChannel = (SocketChannel) channelHandlerContext.channel();
        String channelId = socketChannel.id().toString();

        socketChannel.writeAndFlush(new LoginMsg());
        
        //先检查是否已登录，得到服务器的鉴权
        if (MsgType.LOGIN.equals(baseMsg.getType())) {
            String userName = PropertiesUtil.readProperties("USER_NAME");
            String password = PropertiesUtil.readProperties("PASSWORD");
            LoginMsg loginMsg = (LoginMsg) baseMsg;
            //请求的用户名和密码判断
            if (Encrypt.MD5Encode(userName).equals(loginMsg.getUserName()) && Encrypt.MD5Encode(password).equals(loginMsg.getPassword())) {
                //登录成功,由服务器生产clientID和token，
                //                String clientId = UniqueIdUtil.getGuid();
                //                loginMsg.setClientId(clientId);

                //把channel存到服务端的map中
                ObdChannelMap.add(channelId, socketChannel);

                ReplyClientLoginBody replyLoginBody = new ReplyClientLoginBody();
                replyLoginBody.setAuthToken(channelId);//token算法
                replyLoginBody.setReturnCode(ReturnCode.LOGIN_SUCCESS);
                ReplyMsg replyMsg = new ReplyMsg();
                replyMsg.setBody(replyLoginBody);
                channelHandlerContext.channel().writeAndFlush(replyMsg);

                System.out.println(String.format("---------%s---登录成功 ", channelId));
            } else {
                System.out.println(String.format("---------%s---登录失败 ", channelId));
            }
        } else {
            if (ObdChannelMap.get(channelId) == null) {
                //说明未登录，或者连接断了，服务器向客户端发起登录请求，让客户端重新登录
                LoginMsg loginMsg = new LoginMsg();
                socketChannel.writeAndFlush(loginMsg);
                System.out.println("-----------未登录或断开连接了");
            } else {
                System.out.println("-----------已经登录并获得服务器的鉴权");
            }
        }

        //处理各种消息
        switch (baseMsg.getType())
        {
            case PING://收到客户端的心跳消息
                //                PingMsg pingMsg = (PingMsg) baseMsg;
                PingMsg replyPing = new PingMsg();
                //回复客服端心跳消息
                ObdChannelMap.get(channelId).writeAndFlush(replyPing);
                System.out.println("--------------Server send reply ping msg");
                break;
            case ASK: //收到客户端的请求
                AskMsg askMsg = (AskMsg) baseMsg;
                //登录时候服务器产生的token值
                if (channelId.equals(askMsg.getParams().getAuth())) {
                    //获取业务代码,处理不同业务逻辑
                    BusinessCode code = askMsg.getCode();
                    switch (code)
                    {
                        case GPS_UPLOAD:

                            break;
                        case OBD_UPLOAD:

                            break;
                        case DRIVE_HABIT_UPLOAD:

                            break;
                        case DATA_UPLOAD:

                            break;
                        default:
                            ReplyClientBody replyClientBody = new ReplyClientBody("replyClientBody!!!");
                            replyClientBody.setReturnCode(ReturnCode.RECIEVE_SUCCESS);
                            ReplyMsg replyMsg = new ReplyMsg();
                            replyMsg.setBody(replyClientBody);
                            ObdChannelMap.get(channelId).writeAndFlush(replyMsg);
                            System.out.println("---------ASK");
                            break;
                    }
                } else {
                    System.out.println("---------接受到非法请求消息，消息被拒绝处理!");
                }
                break;
            case REPLY://收到客户端回复
                ReplyMsg replyMsg = (ReplyMsg) baseMsg;
                ReplyServerBody replyServerBody = (ReplyServerBody) replyMsg.getBody();
                System.out.println("receive client msg: " + replyServerBody.getReplyServerInfo());
                break;
            default:
                break;
        }
        ReferenceCountUtil.release(baseMsg);
    }
}
