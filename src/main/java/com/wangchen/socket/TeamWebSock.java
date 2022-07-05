package com.wangchen.socket;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Slf4j
@ServerEndpoint("/teamWebsocket/{openId}/{tableId}/{sitId}")
@Component
public class TeamWebSock {
    //该连接的属性
    private TeamGameManager TGM = TeamGameManager.getInstance();//团队赛游戏管理类
    private Session session;
    private String open_id;

    @OnOpen
    public void onOpen(@PathParam("openId") String openId, @PathParam("tableId") int tableId, @PathParam("sitId") int sitId, Session session) {
        try {
            this.session = session;
            this.open_id = openId;
            log.debug("team connect onOpen:openId:{}, tableId:{}, sitId:{}, session:{}", openId, tableId, sitId, session);

            //增加在线玩家
            TGM.addPlayer(openId,tableId,sitId,this);

        } catch (IOException e) {
            log.error("team onOpen error:{}", e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            log.debug("{} team message:{}, session:{}", open_id, message, session);
            if (!JSONObject.isValid(message)) return;
            JSONObject json = JSONObject.parseObject(message);
            String cmd = json.getString("cmd");
            JSONObject data = json.getJSONObject("data");
            if ("ping".equals((cmd))) {
                //发送 pong
                JSONObject obj = new JSONObject(); obj.put("cmd","pong");
                this.sendMessage(obj);
                return;
            }
            if ("chat".equals((cmd))) {
                TGM.chat(open_id,data.getString("msg"));
                return;
            }
            if ("sit".equals((cmd))) {
                TGM.sit(open_id,data.getIntValue("table_id"),data.getIntValue("sit_id"),this);
            }
            if ("stand".equals((cmd))) {
                TGM.stand(open_id);
            }
            if ("kick".equals((cmd))) {
                TGM.kick(open_id,data.getIntValue("table_id"),data.getIntValue("sit_id"),this);
            }
            if ("start".equals((cmd))) {
                TGM.start(open_id,this);
            }
            if ("select".equals((cmd))) {
                TGM.select(open_id,data.getIntValue("topic_num"),data.getIntValue("s_num"),this);
            }
            if ("time_out".equals((cmd))) {
                TGM.timeOut(open_id,data.getIntValue("topic_num"),this);
            }

        } catch (IOException e) {
            log.error("team onMessage error:{}", e.getMessage());
        }
    }

    @OnError
    public void onError(Throwable throwable) {
        log.error("team error message:{}\n stack:{}\n session:{}", throwable.getMessage(), throwable.getStackTrace(), session);
    }

    @OnClose
    public void onClose() {
        try {
            log.debug("team disconnect onClose:openId:{}, session:{}", open_id, session);

            //删除在线玩家
            TGM.deletePlayer(open_id);
        } catch (IOException e) {
            log.error("team onClose error:{}", e.getMessage());
        }
    }

    public void sendMessage(JSONObject object){
        if (session.isOpen()){
            try {
                session.getBasicRemote().sendText(toJSONString(object));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String toJSONString(Object object){
        return JSONObject.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }

}

