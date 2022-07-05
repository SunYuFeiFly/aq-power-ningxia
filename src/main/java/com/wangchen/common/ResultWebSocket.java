package com.wangchen.common;

import lombok.Data;

@Data
public class ResultWebSocket extends Result {

    /** 房间号 */
    private long room;

    /** -1:未答题 1:题目答对，2:题目答错 */
    private int is_answer;

    /** 0:准备中 1:游戏中 2:本题答完 3:全局结束  */
    private int gameState;

    /** 是否房主 1:房主  0房间玩家 */
    private int isHouseOwner;

    /** 题目Id */
    private int topicId;

    /** 结算获取到的成就值 */
    private int getExperienceNum;

    /** 结算获取到的塔币值 */
    private int getCoinNum;

    /** 是否处于匹配状态，默认false */
    private boolean isPiPei;

    /** 本人 */
    UserResult userResult;

    /** 一般都是房主 */
    UserResult blueUserResult;

    /** 一般都是被拉人 或者匹配的人 */
    UserResult redUserResult;


    @Override
    public ResultWebSocket clone() {
        ResultWebSocket result = new ResultWebSocket();
        result.room = this.room;
        result.is_answer = this.is_answer;
        result.gameState = this.gameState;
        result.isHouseOwner = this.isHouseOwner;
        result.topicId = this.topicId;
        result.userResult = this.userResult;
        result.blueUserResult = this.blueUserResult;
        result.redUserResult = this.redUserResult;
        result.isPiPei = this.isPiPei;
        result.getExperienceNum = this.getExperienceNum;
        result.getCoinNum = this.getCoinNum;
        return result;
    }

}
