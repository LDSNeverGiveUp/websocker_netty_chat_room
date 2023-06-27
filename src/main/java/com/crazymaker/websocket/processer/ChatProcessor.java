package com.crazymaker.websocket.processer;

import com.crazymaker.websocket.Model.User;
import com.crazymaker.websocket.session.ServerSession;
import com.crazymaker.websocket.util.JsonUtil;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务处理器
 *
 * @author lidongsheng
 */
@Slf4j
public class ChatProcessor {

    /**
     * 单例
     */
    private static final ChatProcessor singleInstance = new ChatProcessor();

    public static ChatProcessor inst() {
        return singleInstance;
    }

    /**
     * 连接建立成功调用的方法
     *
     * @param s 会话
     */
    public String onOpen(ServerSession s) throws IOException {
        Map<String, String> result = new HashMap<>();
        result.put("type", "bing");
        result.put("sendUser", "系统消息");
        result.put("id", s.getId());

        String json = JsonUtil.pojoToJson(result);
        return json;
    }

    /**
     * 连接关闭调用的方法
     */
    public String onClose(ServerSession s) {
        User user = s.getUser();
        if (user != null) {
            String nick = user.getNickname();
            Map<String, String> result = new HashMap<>();
            result.put("type", "init");
            result.put("msg", nick + "离开房间");
            result.put("sendUser", "系统消息");
            String json = JsonUtil.pojoToJson(result);
            return json;
        }
        return null;
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 消息内容
     * @param session 会哈
     */
    public Map<String, String> onMessage(String message, ServerSession session) {
        System.out.println("收到消息：" + message);
        TypeToken typeToken = new TypeToken<HashMap<String, String>>() {
        };
        Map<String, String> map = JsonUtil.jsonToPojo(message, typeToken);
        Map<String, String> result = new HashMap<>();
        User user;
        switch (map.get("type")) {
            case "msg":
                user = session.getUser();
                result.put("type", "msg");
                result.put("msg", map.get("msg"));
                result.put("sendUser", user.getNickname());
                break;
            case "init":
                String room = map.get("room");
                session.setGroup(room);
                String nick = map.get("nick");
                user = new User(session.getId(), nick);
                session.setUser(user);
                result.put("type", "init");
                result.put("msg", nick + "成功加入房间");
                result.put("sendUser", "系统消息");

                break;
            case "ping":
                break;
        }
        return result;
    }

    /**
     * 连接发生错误时的调用方法
     *
     * @param session 会话
     * @param error   异常
     */
    public String onError(ServerSession session, Throwable error) {

        //捕捉异常信息
        if (null != error) {
            log.error(error.getMessage());
        }

        User user = session.getUser();
        if (user == null) {
            return null;
        }
        String nick = user.getNickname();

        Map<String, String> result = new HashMap<>();
        result.put("type", "init");
        result.put("msg", nick + "离开房间");
        result.put("sendUser", "系统消息");

        String json = JsonUtil.pojoToJson(result);
        return json;
    }


}