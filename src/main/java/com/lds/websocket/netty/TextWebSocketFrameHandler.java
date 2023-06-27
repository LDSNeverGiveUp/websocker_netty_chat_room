package com.lds.websocket.netty;

import com.lds.websocket.event.SecurityCheckSuccessEvent;
import com.lds.websocket.processer.ChatProcessor;
import com.lds.websocket.session.ServerSession;
import com.lds.websocket.session.SessionMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * WebSocket 帧：WebSocket 以帧的方式传输数据，每一帧代表消息的一部分。一个完整的消息可能会包含许多帧
 * @author lidongsheng
 */
@Slf4j
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //增加消息的引用计数（保留消息），并将他写到 ChannelGroup 中所有已经连接的客户端

        // 1 通过ChannelHandlerContext获取到对应的ServerSession

        ServerSession session = ServerSession.getSession(ctx);

        //todo 2 对消息数据进行处理并给出回执
        Map<String, String> result = ChatProcessor.inst().onMessage(msg.text(), session);

        if (result != null && null != result.get("type")) {
            switch (result.get("type")) {
                case "msg":
                    SessionMap.inst().sendToOthers(result, session);
                    break;
                case "init":
                    SessionMap.inst().addSession(result, session);
                    break;
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //1 是否握手成功，升级为 Websocket 协议
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            // 握手成功，移除 HttpRequestHandler，因此将不会接收到任何消息
            // 并把握手成功的 Channel 加入到 ChannelGroup 中
            log.info("握手成功！！！！");
            ServerSession session = new ServerSession(ctx.channel());
            String echo = ChatProcessor.inst().onOpen(session);
            SessionMap.inst().sendMsg(ctx, echo);

        } else if (evt instanceof IdleStateEvent)
        // 2 心跳消息
        {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if (stateEvent.state() == IdleState.READER_IDLE) {
                ServerSession session = ServerSession.getSession(ctx);
                SessionMap.inst().remove(session);
                session.processError(null);
            }
        } else if (evt instanceof SecurityCheckSuccessEvent) {
            // 3 前置token校验
            log.info("token校验通过，开始进行session的创建并发送init 消息");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


}
