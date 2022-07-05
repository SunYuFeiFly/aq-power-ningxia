package com.wangchen.common;

import lombok.Data;

import java.util.List;

/**
 * @Description: 好友约战房间
 * @Author: chengzhang
 * @Date: 2019/12/3 15:00
 * @Version: 1.0
 */
@Data
public class ThreeResultWebSocket extends Result{
    private long room;
//    private int is_answer;//-1正在等待玩家进入房间 0为未答题完，1为当前题都答完了，2为所有题都答完了
    private int gameState;//0准备中 1游戏中 2本题答完  3全局结束;
    private int isHouseOwner;//是否房主 0:房主  1房间玩家
    private int isNowAnswer;//当前答题人 0:不是 1:是

//    private int topicId;//当前是答那题

    private int allScore;//总得分

    ThreeUserResult userResult; // 本人
    List<ThreeUserResult> userList; //所有玩家集合

    private Integer teamRank;//团队名次

    public ThreeResultWebSocket clone(){
        ThreeResultWebSocket result = new ThreeResultWebSocket();
        result.room = this.room;
        result.isHouseOwner = this.isHouseOwner;
        result.isNowAnswer = this.isNowAnswer;
        result.userList = this.userList;
        return result;
    }
}
