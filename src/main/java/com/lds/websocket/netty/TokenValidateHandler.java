package com.lds.websocket.netty;

import com.lds.websocket.event.SecurityCheckSuccessEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * token的校验
 *
 * @author lidongsheng
 */
@Slf4j
public class TokenValidateHandler extends ChannelInboundHandlerAdapter {
    private final String token = "barrer:";
    private AttributeKey<SecurityCheckSuccessEvent> SECURITY_CHECK_COMPLETE_ATTRIBUTE_KEY = AttributeKey.valueOf("SECURITY_CHECK_COMPLETE_ATTRIBUTE_KEY");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpMessage) {
            HttpHeaders headers = ((FullHttpMessage) msg).headers();
            String token = headers.get("token");
            // token为空
            if (token == null) {
                log.info("header is null ,自定义事件发布");
                SecurityCheckSuccessEvent securityCheckSuccessEvent = new SecurityCheckSuccessEvent();
                securityCheckSuccessEvent.setUserId(UUID.randomUUID().toString());
                ctx.channel().attr(SECURITY_CHECK_COMPLETE_ATTRIBUTE_KEY).set(securityCheckSuccessEvent);
                // todo 发布事件
                ctx.fireUserEventTriggered(securityCheckSuccessEvent);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
