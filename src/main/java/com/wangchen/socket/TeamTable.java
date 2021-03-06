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

    private TeamGameManager TGM = TeamGameManager.getInstance();//????????????????????????

    private int table_id;
    public int topic_num = 0;//????????? 0-9
    private int[] is_answered = new int[]{-1,-1,-1,-1,-1,-1};//??????????????????????????????
    private int[] right_answer_arr = new int[]{0,0,0,0,0,0};//??????????????????????????????
    private int[] score_arr = new int[]{0,0,0,0,0,0};//????????????????????????????????????
    private int state = 0;//?????? 0?????? 1??????
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

    //??????????????????
    public void start(){
        //????????????
        if(state==1){
            log.error("????????????");
            return;
        }
        //????????????
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
                JSONObject data_obj = new JSONObject(); data_obj.put("msg","????????????");
                JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
                user_arr[0].getTws().sendMessage(obj);
            }
            return;
        }
        //??????????????????
        int companyType = 1;
        if(user_arr[0]!=null){
            companyType = user_arr[0].getType();
        }
        List<BranchTopic> topicListTopic = branchTopicService.listTopicRandomByCompanyType(ALL_ANSWER_NUM,companyType);

        if(topicListTopic.size()<ALL_ANSWER_NUM){
            log.error("??????????????????");
            return;
        }
        topic_list = new ArrayList<OneVsOneTopicVo>();
        for(int i=1; i<=topicListTopic.size();i++){
            OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
            BeanUtils.copyProperties(topicListTopic.get(i-1),oneVsOneTopicVo);
            oneVsOneTopicVo.setTypeName(Constants.newBuMenKu.get(oneVsOneTopicVo.getType())[1]);
            oneVsOneTopicVo.setRankNo(i);
            oneVsOneTopicVo.setOptionList(branchOptionService.list(new QueryWrapper<BranchOption>()
                    .eq("topic_id",oneVsOneTopicVo.getId())));//????????????
            topic_list.add(oneVsOneTopicVo);
        }
        //?????? start
        JSONObject data_obj = new JSONObject(); data_obj.put("topic_list",topic_list); data_obj.put("user_list",user_arr);
        JSONObject obj = new JSONObject(); obj.put("cmd","start"); obj.put("data",data_obj);
        groupCastMessage(obj);

        //table??????
        state = 1;
        //????????????????????????????????????
        for(int i=0;i<6;i++){
            is_answered[i]=-1;
        }
        //?????? hall_state
        data_obj = new JSONObject(); data_obj.put("table_id",table_id); data_obj.put("state",state);
        obj = new JSONObject(); obj.put("cmd","hall_state"); obj.put("data",data_obj);
        TGM.broadCastMessage(obj,null);
    }

    //???????????????
    public synchronized void select(int topicNum, int sNum, int sitId, boolean isQuitSelect){
        //????????????
        if(state==0){
            log.info("team select ???????????? ???????????????1????????????timeOut");
            return;
        }
        //?????????????????????
        if(topicNum!=topic_num){
            log.info("team select ???????????? ???????????????1????????????timeOut");
            return;
        }
        //?????????????????????
        if(is_answered[sitId]!=-1){
            log.error("team select ???????????????");
            return;
        }
        //????????????
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
        int now_p_count = blue_p_count + red_p_count;//???????????????
        //????????????
        if(blue_p_count<1||red_p_count<1){
            return;
        }
        //??????????????????
        is_answered[sitId] = sNum;//????????????
        int answer_count = 0;//?????????????????????
        for(int i=0;i<6;i++){
            if(is_answered[i]!=-1){
                answer_count++;
            }
        }
        int left_answer_num = now_p_count - answer_count;//???????????????????????????(5,4,3,2,1,0)

        int score = 0;//??????????????????
        //????????????????????????(0-3),????????????????????????sNum?????????
        int right_num = -1;
        if(topic_list.get(topic_num).getTopicType()==0){//?????????
            for(int i=0;i<topic_list.get(topic_num).getOptionList().size();i++){//????????????
                if(topic_list.get(topic_num).getOptionList().get(i).getId().equals(topic_list.get(topic_num).getCorrectOptionId())){
                    right_num = i;
                    break;
                }
            }
        }else if(topic_list.get(topic_num).getTopicType()==2){//?????????
            if(topic_list.get(topic_num).getOptionList().get(0).getContent().equals("???")){
                right_num = 0;
            }else{
                right_num = 1;
            }
        }
        //????????????
        int is_right = 0;
        if(right_num == sNum){//?????????
            is_right = 1;
            if(sitId<3){//???????????????
                score += 120/blue_p_count;//120 60 40
            }else{//???????????????
                score += 120/red_p_count;//120 60 40
            }
            right_answer_arr[sitId] += 1;//????????????????????????????????????
        }
        //??????????????????(???:??????5?????????????????????????????????50%)
        score = score + (score * left_answer_num)/10;
        //??????????????????
        if(sitId<3){//???????????????
            blue_all_score += score;
        }else{//???????????????
            red_all_score += score;
        }
        score_arr[sitId] += score;//?????????????????????

        //?????? user_answer
        JSONObject data_obj = new JSONObject();
        data_obj.put("topic_num",topic_num); data_obj.put("sit_id",sitId); data_obj.put("s_num",sNum);
        data_obj.put("is_right",is_right); data_obj.put("score",score);
        JSONObject obj = new JSONObject(); obj.put("cmd","user_answer"); obj.put("data",data_obj);
        groupCastMessage(obj);

        //?????????????????????????????????????????????
        boolean is_all_answered = true;
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null){
                if(is_answered[i] == -1){
                    is_all_answered = false;
                    break;
                }
            }
        }
        if(is_all_answered){//??????????????????????????????
            //??????????????????
            List<JSONObject> s_num_list = new ArrayList<JSONObject>();
            for(int i=0;i<6;i++){
                if(user_arr[i]!=null){
                    if(is_answered[i]!=-1) {
                        if(i==sitId && isQuitSelect) {//?????????????????????select????????????????????????????????????????????????????????????
                            log.debug("?????????????????????select???????????????????????????????????????????????????");
                        }else{
                            JSONObject obj_item = new JSONObject();
                            obj_item.put("sit_id", i);
                            obj_item.put("s_num", is_answered[i]);
                            s_num_list.add(obj_item);
                        }
                    }
                }
            }
            //?????? answer
            data_obj = new JSONObject();
            data_obj.put("topic_num",topic_num);
            data_obj.put("s_num_list",s_num_list); data_obj.put("right_num",right_num);
            obj = new JSONObject(); obj.put("cmd","answer"); obj.put("data",data_obj);
            groupCastMessage(obj);

            //?????????????????????

            //??????????????????
            topic_num = topic_num+1;

            //????????????????????????????????????
            for(int i=0;i<6;i++){
                is_answered[i]=-1;
            }

            //??????????????????
            if(topic_num==ALL_ANSWER_NUM){
                //??????????????????
                endGame();
            }
        }
    }

    //??????
    public synchronized void timeOut(int topicNum, int sitId){
        //?????????????????????
        if(topicNum!=topic_num){
            log.info("team timeOut ???????????? ???????????????1????????????timeOut");
            return;
        }
        //????????????
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null){
                if(is_answered[i]==-1){//??????????????????
                    select(topicNum, 99, i,false);//?????????????????????????????????
                }
            }
        }
    }

    //??????????????????
    public synchronized void playerQuit(String openId, int sitId){
        if(!user_arr[sitId].getOpenId().equals(openId)){
            log.error("team table playerQuit error, {}, {}",openId,sitId);
            return;
        }
        if(state==0){//??????????????????,????????????
            return;
        }

        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if(is_answered[sitId]==-1){//???????????????
            select(topic_num, 98, sitId,true);//?????????????????????????????????
        }

        //????????????????????????????????????(???????????????,?????????????????????????????????)
        UserTeamVsTeamLog userTeamVsTeamLog = new UserTeamVsTeamLog();
        userTeamVsTeamLog.setOpenId(openId);
        userTeamVsTeamLog.setIsWin(2);
        userTeamVsTeamLog.setCreateDate(new Date());
        userTeamVsTeamLog.setCreateTime(new Date());
        userTeamVsTeamLog.setRightAnswerNum(right_answer_arr[sitId]);
        userTeamVsTeamLog.setAllAnswerNum(topic_num+1);
        userTeamVsTeamLogService.save(userTeamVsTeamLog);
        //??????????????????
        int gameCount = user_arr[sitId].getTeamGameCountToday();
        user_arr[sitId].setTeamGameCountToday(++gameCount);
        if(user_arr[sitId].getTeamGameCountToday()==1){//??????????????????????????????
            //?????????????????????????????? (???????????????????????????)
            com.wangchen.entity.User user = userService.getUserByOpenId(openId);
            if (user != null) { computeUtils.computeGame2(openId,5,0,user); }
        }
        //?????? info
        JSONObject data_obj = new JSONObject();
        data_obj.put("msg",(sitId<3?"??????":"??????")+"??????:"+user_arr[sitId].getName()+"???????????????");
        JSONObject obj = new JSONObject(); obj.put("cmd","info"); obj.put("data",data_obj);
        groupCastMessage(obj);

        //????????????
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
        //?????????????????????????????????
        if(sitId<3){
            blue_p_count--;
        }else{
            red_p_count--;
        }
        int now_p_count = blue_p_count + red_p_count;//???????????????
        //????????????
        if(blue_p_count<1||red_p_count<1){//????????????????????????
            try {
                if(sitId<3){blue_all_score=0;}else{red_all_score=0;}//???????????????0
                TGM.stand(openId);//???????????????????????????(???????????????TGM.deletePlayer()??????stand??????)
            } catch (IOException e) {
                e.printStackTrace();
            }
            endGame();
        }
    }

    //????????????????????????
    public synchronized void endGame(){
        if(state==0){
            return;
        }
        //table??????
        state = 0;
        //???????????????????????????????????????
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

        //???????????????
        //1 - ?????????????????????????????????
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
                //??????????????????
                int gameCount = user_arr[i].getTeamGameCountToday();
                user_arr[i].setTeamGameCountToday(++gameCount);
                if(user_arr[i].getTeamGameCountToday()==1){//??????????????????????????????
                    //???????????????????????????????????????
                    com.wangchen.entity.User user = userService.getUserByOpenId(user_arr[i].getOpenId());
                    if (user != null) { computeUtils.computeGame2(user_arr[i].getOpenId(),(i<3)?blue_exp:red_exp,(i<3)?blue_coin:red_coin,user); }
                }
            }
        }
        //2 - ????????????????????? (?????????????????????????????????????????????????????????)
        UserTeamTableLog userTeamTableLog = new UserTeamTableLog();
        userTeamTableLog.setCreateDate(new Date());
        userTeamTableLogService.save(userTeamTableLog);

        //?????? end
        List<JSONObject> reward_list = new ArrayList<JSONObject>();
        for(int i=0;i<6;i++){
            if(user_arr[i]!=null) {
                JSONObject obj_item = new JSONObject();
                obj_item.put("sit_id", i);
                obj_item.put("user", user_arr[i]);
                if(user_arr[i].getTeamGameCountToday()==1){//??????????????????
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

        //??????????????????
        for(TeamUser item : user_arr){
            if(item!=null){
                try {
                    TGM.stand(item.getOpenId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //??????????????????
        topic_list = null;
        blue_all_score = 0;
        red_all_score = 0;
        topic_num = 0;
        for(int i=0;i<6;i++){
            is_answered[i] = -1;//??????????????????????????????
            right_answer_arr[i] = 0;//??????????????????????????????
            score_arr[i] = 0;//????????????????????????????????????
        }

        //?????? hall_state
        data_obj = new JSONObject(); data_obj.put("table_id",table_id); data_obj.put("state",state);
        obj = new JSONObject(); obj.put("cmd","hall_state"); obj.put("data",data_obj);
        TGM.broadCastMessage(obj,null);
    }

    //????????????
    public void groupCastMessage(JSONObject object){
        for(TeamUser item : user_arr){
            if(item!=null){
                item.getTws().sendMessage(object);
            }
        }
    }
}
