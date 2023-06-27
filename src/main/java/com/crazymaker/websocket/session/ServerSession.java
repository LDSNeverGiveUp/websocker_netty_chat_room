package com.crazymaker.websocket.session;

import com.crazymaker.websocket.Model.User;
import com.crazymaker.websocket.processer.ChatProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 实现服务器Socket Session会话
 *
 * @author lidongsheng
 */
@Data
@Slf4j
public class ServerSession {

    public static final AttributeKey<String> KEY_USER_ID =
            AttributeKey.valueOf("key_user_id");

    public static final AttributeKey<ServerSession> SESSION_KEY =
            AttributeKey.valueOf("SESSION_KEY");


    /**
     * 用户实现服务端会话管理的核心
     */
    //通道
    private Channel channel;
    //用户
    private User user;

    //session唯一标示
    private final String sessionId;

    /**
     * 分组name
     */
    private String group;

    //登录状态
    private boolean isLogin = false;

    /**
     * session中存储的session 变量属性值
     */
    private Map<String, Object> map = new HashMap<String, Object>();

    public ServerSession(Channel channel) {
        this.channel = channel;
        this.sessionId = buildNewSessionId();
        log.info(" ServerSession 绑定会话 " + channel.remoteAddress());
        // channel绑定为当前的对象
        channel.attr(ServerSession.SESSION_KEY).set(this);
    }

    //反向导航（通过ChannelHandlerContext获取channel然后拿到对应的ServerSession）
    public static ServerSession getSession(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        return channel.attr(ServerSession.SESSION_KEY).get();
    }

    //反向导航
    public static ServerSession getSession(Channel channel) {
        return channel.attr(ServerSession.SESSION_KEY).get();
    }


    public String getId() {
        return sessionId;
    }

    private static String buildNewSessionId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    public synchronized void set(String key, Object value) {
        map.put(key, value);
    }


    public synchronized <T> T get(String key) {
        return (T) map.get(key);
    }


    public boolean isValid() {
        return getUser() != null ? true : false;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public void processError(Throwable error) {

        /**
         * 处理错误，得到处理结果
         */
        String result = ChatProcessor.inst().onError(this, error);
        /**
         * 发送处理结果到其他的组内用户
         */
        SessionMap.inst().sendToAll(result, this);


        String echo = ChatProcessor.inst().onClose(this);
        /**
         * 关闭连接， 关闭前发送一条通知消息
         */

        SessionMap.inst().closeSession(this, echo);


    }
}
