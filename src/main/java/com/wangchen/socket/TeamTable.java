package com.wangchen.socket;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.ComputeUtils;
import com.wangchen.vo.OneVsOneTopicVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Data
@Component
public class TeamTable {

    private static BranchTopicService branchTopicService;
    @Autowired
    public void setBranchTopicService(BranchTopicService branchTopicService) {
        TeamTable.branchTopicService = branchTopicService;
    }

    private static BranchOptionService branchOptionService;
    @Autowired
    public void setBranchOptionService(BranchOptionService branchOptionService) {
        TeamTable.branchOptionService = branchOptionService;
    }

    private static UserTeamVsTeamLogService userTeamVsTeamLogService;
    @Autowired
    public void setUserTeamVsTeamLogService(UserTeamVsTeamLogService userTeamVsTeamLogService) {
        TeamTable.userTeamVsTeamLogService = userTeamVsTeamLogService;
    }

    private static UserTeamTableLogService userTeamTableLogService;
    @Autowired
    public void setUserTeamTableLogService(UserTeamTableLogService userTeamTableLogService) {
        TeamTable.userTeamTableLogService = userTeamTableLogService;
    }
    private static ComputeUtils computeUtils;
    @Autowired
    public void setComputeUtils(ComputeUtils computeUtils){
        TeamTable.computeUtils = computeUtils;
    }

    private static UserService userService;
    @Autowired
    public void setUserService(UserService userService){
        TeamTable.userService = userService;
    }

    private TeamGameManager TGM = TeamGameManager.getInstance();//团队赛游戏管理类

    private int table_id;
    public int topic_num = 0;//第几题 0-9
    private int[] is_answered = new int[]{-1,-1,-1,-1,-1,-1};//当前题目已经作答情况
    private int[] right_answer_arr = new int[]{0,0,0,0,0,0};//所有玩家答题正确数量
    private int[] score_arr = new int[]{0,0,0,0,0,0};//所有玩家每局游戏的总得分
    private int state = 0;//状态 0准备 1战斗
    private TeamUser[] user_arr = new TeamUser[6];
    private List<OneVsOneTopicVo> topic_list = new ArrayList<OneVsOneTopicVo>();
    private int blue_all_score = 0;
    private int red_all_score = 0;

    private int ALL_ANSWER_NUM = 10;

    public TeamTable(){
        super();
    }
    public TeamTable(int tableId){
        table_id = tableId;
    }

    //房主开始游戏
    public void start(){
        //判断状态
        if(state==1){
            log.error("已经开战");
            return;
        }
        //判断人数
        int blue_p_count = 0;
        int red_p_count = 0;
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null && i<3){
               blue_p_count++;
            }
            if(user_arr[i]!=null && i>=3){
                red_p_count++;
            }
        }
        if(blue_p_count<2||red_p_count<2){
            if(user_arr[0]!=null){
                JSONObject data_obj = new JSONObject(); data_obj.put("msg","人数不足");
                JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
                user_arr[0].getTws().sendMessage(obj);
            }
            return;
        }
        //获取题目信息
        int companyType = 1;
        if(user_arr[0]!=null){
            companyType = user_arr[0].getType();
        }
        List<BranchTopic> topicListTopic = branchTopicService.listTopicRandomByCompanyType(ALL_ANSWER_NUM,companyType);

        if(topicListTopic.size()<ALL_ANSWER_NUM){
            log.error("题目数量不够");
            return;
        }
        topic_list = new ArrayList<OneVsOneTopicVo>();
        for(int i=1; i<=topicListTopic.size();i++){
            OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
            BeanUtils.copyProperties(topicListTopic.get(i-1),oneVsOneTopicVo);
            oneVsOneTopicVo.setTypeName(Constants.newBuMenKu.get(oneVsOneTopicVo.getType())[1]);
            oneVsOneTopicVo.setRankNo(i);
            oneVsOneTopicVo.setOptionList(branchOptionService.list(new QueryWrapper<BranchOption>()
                    .eq("topic_id",oneVsOneTopicVo.getId())));//答案信息
            topic_list.add(oneVsOneTopicVo);
        }
        //组播 start
        JSONObject data_obj = new JSONObject(); data_obj.put("topic_list",topic_list); data_obj.put("user_list",user_arr);
        JSONObject obj = new JSONObject(); obj.put("cmd","start"); obj.put("data",data_obj);
        groupCastMessage(obj);

        //table状态
        state = 1;
        //重置当前题目每个人的选项
        for(int i=0;i<6;i++){
            is_answered[i]=-1;
        }
        //广播 hall_state
        data_obj = new JSONObject(); data_obj.put("table_id",table_id); data_obj.put("state",state);
        obj = new JSONObject(); obj.put("cmd","hall_state"); obj.put("data",data_obj);
        TGM.broadCastMessage(obj,null);
    }

    //玩家做选择
    public synchronized void select(int topicNum, int sNum, int sitId, boolean isQuitSelect){
        //判断状态
        if(state==0){
            log.info("team select 还没开战 或同时收到1个以上的timeOut");
            return;
        }
        //判断是不是该题
        if(topicNum!=topic_num){
            log.info("team select 题号不对 或同时收到1个以上的timeOut");
            return;
        }
        //判断是否答题过
        if(is_answered[sitId]!=-1){
            log.error("team select 已经答过题");
            return;
        }
        //判断人数
        int blue_p_count = 0;
        int red_p_count = 0;
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null && i<3){
                blue_p_count++;
            }
            if(user_arr[i]!=null && i>=3){
                red_p_count++;
            }
        }
        int now_p_count = blue_p_count + red_p_count;//当前总人数
        //人数不足
        if(blue_p_count<1||red_p_count<1){
            return;
        }
        //处理答题逻辑
        is_answered[sitId] = sNum;//标记已答
        int answer_count = 0;//有几个人作答了
        for(int i=0;i<6;i++){
            if(is_answered[i]!=-1){
                answer_count++;
            }
        }
        int left_answer_num = now_p_count - answer_count;//剩下几个人没有作答(5,4,3,2,1,0)

        int score = 0;//该人本题得分
        //获得本题正确选项(0-3),用于跟用户发来的sNum做比对
        int right_num = -1;
        if(topic_list.get(topic_num).getTopicType()==0){//选择题
            for(int i=0;i<topic_list.get(topic_num).getOptionList().size();i++){//循环选项
                if(topic_list.get(topic_num).getOptionList().get(i).getId().equals(topic_list.get(topic_num).getCorrectOptionId())){
                    right_num = i;
                    break;
                }
            }
        }else if(topic_list.get(topic_num).getTopicType()==2){//判断题
            if(topic_list.get(topic_num).getOptionList().get(0).getContent().equals("对")){
                right_num = 0;
            }else{
                right_num = 1;
            }
        }
        //判断对错
        int is_right = 0;
        if(right_num == sNum){//答对了
            is_right = 1;
            if(sitId<3){//该人是蓝队
                score += 120/blue_p_count;//120 60 40
            }else{//该人是红队
                score += 120/red_p_count;//120 60 40
            }
            right_answer_arr[sitId] += 1;//记录每位玩家共答对了几题
        }
        //答题顺序加成(例:剩余5人就在基础得分基础上多50%)
        score = score + (score * left_answer_num)/10;
        //处理两队总分
        if(sitId<3){//该人是蓝队
            blue_all_score += score;
        }else{//该人是红队
            red_all_score += score;
        }
        score_arr[sitId] += score;//记录个人总得分

        //组播 user_answer
        JSONObject data_obj = new JSONObject();
        data_obj.put("topic_num",topic_num); data_obj.put("sit_id",sitId); data_obj.put("s_num",sNum);
        data_obj.put("is_right",is_right); data_obj.put("score",score);
        JSONObject obj = new JSONObject(); obj.put("cmd","user_answer"); obj.put("data",data_obj);
        groupCastMessage(obj);

        //判断当前题目所有人是否答题完毕
        boolean is_all_answered = true;
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null){
                if(is_answered[i] == -1){
                    is_all_answered = false;
                    break;
                }
            }
        }
        if(is_all_answered){//当最后一个人答题完毕
            //处理本题结果
            List<JSONObject> s_num_list = new ArrayList<JSONObject>();
            for(int i=0;i<6;i++){
                if(user_arr[i]!=null){
                    if(is_answered[i]!=-1) {
                        if(i==sitId && isQuitSelect) {//当玩家退出触发select时，他自己的答案不发给小程序，否则会报错
                            log.debug("该玩家退出出发select，最终所有人的答题结果不放他的结果");
                        }else{
                            JSONObject obj_item = new JSONObject();
                            obj_item.put("sit_id", i);
                            obj_item.put("s_num", is_answered[i]);
                            s_num_list.add(obj_item);
                        }
                    }
                }
            }
            //组播 answer
            data_obj = new JSONObject();
            data_obj.put("topic_num",topic_num);
            data_obj.put("s_num_list",s_num_list); data_obj.put("right_num",right_num);
            obj = new JSONObject(); obj.put("cmd","answer"); obj.put("data",data_obj);
            groupCastMessage(obj);

            //为下一题做准备

            //改变当前题号
            topic_num = topic_num+1;

            //重置当前题目每个人的选项
            for(int i=0;i<6;i++){
                is_answered[i]=-1;
            }

            //当是最后一题
            if(topic_num==ALL_ANSWER_NUM){
                //处理本局结果
                endGame();
            }
        }
    }

    //超时
    public synchronized void timeOut(int topicNum, int sitId){
        //判断是不是该题
        if(topicNum!=topic_num){
            log.info("team timeOut 题号不对 或同时收到1个以上的timeOut");
            return;
        }
        //处理超时
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null){
                if(is_answered[i]==-1){//有人没有作答
                    select(topicNum, 99, i,false);//让这些玩家选择错误选项
                }
            }
        }
    }

    //玩家退出该桌
    public synchronized void playerQuit(String openId, int sitId){
        if(!user_arr[sitId].getOpenId().equals(openId)){
            log.error("team table playerQuit error, {}, {}",openId,sitId);
            return;
        }
        if(state==0){//如果还没开战,不做操作
            return;
        }

        //处理这个玩家的答题情况（假设该桌只有这个玩家没有答题，但是他退出了，要防止出现错误）
        if(is_answered[sitId]==-1){//他没有作答
            select(topic_num, 98, sitId,true);//让这个玩家选择错误选项
        }

        //处理退出玩家的数据持久化(退出就算输,即便他的退出导致了结算)
        UserTeamVsTeamLog userTeamVsTeamLog = new UserTeamVsTeamLog();
        userTeamVsTeamLog.setOpenId(openId);
        userTeamVsTeamLog.setIsWin(2);
        userTeamVsTeamLog.setCreateDate(new Date());
        userTeamVsTeamLog.setCreateTime(new Date());
        userTeamVsTeamLog.setRightAnswerNum(right_answer_arr[sitId]);
        userTeamVsTeamLog.setAllAnswerNum(topic_num+1);
        userTeamVsTeamLogService.save(userTeamVsTeamLog);
        //判断游玩场次
        int gameCount = user_arr[sitId].getTeamGameCountToday();
        user_arr[sitId].setTeamGameCountToday(++gameCount);
        if(user_arr[sitId].getTeamGameCountToday()==1){//只有第一次游戏有奖励
            //加经验值、等级、塔币 (中途退出游戏者算输)
            com.wangchen.entity.User user = userService.getUserByOpenId(openId);
            if (user != null) { computeUtils.computeGame2(openId,5,0,user); }
        }
        //组播 info
        JSONObject data_obj = new JSONObject();
        data_obj.put("msg",(sitId<3?"蓝队":"红队")+"玩家:"+user_arr[sitId].getName()+"离开了游戏");
        JSONObject obj = new JSONObject(); obj.put("cmd","info"); obj.put("data",data_obj);
        groupCastMessage(obj);

        //判断人数
        int blue_p_count = 0;
        int red_p_count = 0;
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null && i<3){
                blue_p_count++;
            }
            if(user_arr[i]!=null && i>=3){
                red_p_count++;
            }
        }
        //人数判定需要减掉该用户
        if(sitId<3){
            blue_p_count--;
        }else{
            red_p_count--;
        }
        int now_p_count = blue_p_count + red_p_count;//当前总人数
        //人数不足
        if(blue_p_count<1||red_p_count<1){//玩家退出导致结算
            try {
                if(sitId<3){blue_all_score=0;}else{red_all_score=0;}//该队总分清0
                TGM.stand(openId);//结算不能包括该玩家(这里会导致TGM.deletePlayer()中的stand无效)
            } catch (IOException e) {
                e.printStackTrace();
            }
            endGame();
        }
    }

    //处理本局游戏结束
    public synchronized void endGame(){
        if(state==0){
            return;
        }
        //table状态
        state = 0;
        //确定每队的经验值和塔币奖励
        int blue_exp;
        int blue_coin;
        int red_exp;
        int red_coin;
        int is_blue_win;
        int is_red_win;
        if(blue_all_score > red_all_score){
            blue_exp = 10;
            blue_coin = 5;
            red_exp = 5;
            red_coin = 0;
            is_blue_win = 1;
            is_red_win = 2;
        }else if(blue_all_score < red_all_score){
            blue_exp = 5;
            blue_coin = 0;
            red_exp = 10;
            red_coin = 5;
            is_blue_win = 2;
            is_red_win = 1;
        }else{
            blue_exp = 10;
            blue_coin = 5;
            red_exp = 10;
            red_coin = 5;
            is_blue_win = 3;
            is_red_win = 3;
        }

        //数据持久化
        //1 - 每个用户的团队赛数据库
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null) {
                UserTeamVsTeamLog userTeamVsTeamLog = new UserTeamVsTeamLog();
                userTeamVsTeamLog.setOpenId(user_arr[i].getOpenId());
                userTeamVsTeamLog.setIsWin((i<3)?is_blue_win:is_red_win);
                userTeamVsTeamLog.setCreateDate(new Date());
                userTeamVsTeamLog.setCreateTime(new Date());
                userTeamVsTeamLog.setRightAnswerNum(right_answer_arr[i]);
                userTeamVsTeamLog.setAllAnswerNum(topic_num);
                userTeamVsTeamLogService.save(userTeamVsTeamLog);
                //判断游玩场次
                int gameCount = user_arr[i].getTeamGameCountToday();
                user_arr[i].setTeamGameCountToday(++gameCount);
                if(user_arr[i].getTeamGameCountToday()==1){//只有第一次游戏有奖励
                    //每个人加经验值、等级、塔币
                    com.wangchen.entity.User user = userService.getUserByOpenId(user_arr[i].getOpenId());
                    if (user != null) { computeUtils.computeGame2(user_arr[i].getOpenId(),(i<3)?blue_exp:red_exp,(i<3)?blue_coin:red_coin,user); }
                }
            }
        }
        //2 - 团队赛桌子数据 (用于后台管理员统计每日团队赛进行了几场)
        UserTeamTableLog userTeamTableLog = new UserTeamTableLog();
        userTeamTableLog.setCreateDate(new Date());
        userTeamTableLogService.save(userTeamTableLog);

        //组播 end
        List<JSONObject> reward_list = new ArrayList<JSONObject>();
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null) {
                JSONObject obj_item = new JSONObject();
                obj_item.put("sit_id", i);
                obj_item.put("user", user_arr[i]);
                if(user_arr[i].getTeamGameCountToday()==1){//判断游玩场次
                    obj_item.put("exp", (i<3)?blue_exp:red_exp);
                    obj_item.put("coin", (i<3)?blue_coin:red_coin);
                }else{
                    obj_item.put("exp", 0);
                    obj_item.put("coin", 0);
                }
                obj_item.put("score", score_arr[i]);
                reward_list.add(obj_item);
            }
        }
        JSONObject data_obj = new JSONObject();
        data_obj.put("reward_list",reward_list);
        data_obj.put("blue_all_score",blue_all_score);
        data_obj.put("red_all_score",red_all_score);
        JSONObject obj = new JSONObject(); obj.put("cmd","end"); obj.put("data",data_obj);
        groupCastMessage(obj);

        //所有玩家起立
        for(TeamUser item : user_arr){
            if(item!=null){
                try {
                    TGM.stand(item.getOpenId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //重制桌子数据
        topic_list = null;
        blue_all_score = 0;
        red_all_score = 0;
        topic_num = 0;
        for(int i=0;i<6;i++){
            is_answered[i] = -1;//当前题目已经作答情况
            right_answer_arr[i] = 0;//所有玩家答题正确数量
            score_arr[i] = 0;//所有玩家每局游戏的总得分
        }

        //广播 hall_state
        data_obj = new JSONObject(); data_obj.put("table_id",table_id); data_obj.put("state",state);
        obj = new JSONObject(); obj.put("cmd","hall_state"); obj.put("data",data_obj);
        TGM.broadCastMessage(obj,null);
    }

    //组播数据
    public void groupCastMessage(JSONObject object){
        for(TeamUser item : user_arr){
            if(item!=null){
                item.getTws().sendMessage(object);
            }
        }
    }
}
