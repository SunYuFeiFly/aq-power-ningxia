package com.wangchen.socket;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class TeamGameManager {

    private static TeamGameManager instance = new TeamGameManager();
    private TeamGameManager(){}
    public static TeamGameManager getInstance(){
        return instance;
    }

    @Autowired
    UserService userService;
    @Autowired
    UserHonorService userHonorService;
    @Autowired
    UserLevelService userLevelService;
    @Autowired
    UserAchievementService userAchievementService;
    @Autowired
    UserTeamVsTeamLogService userTeamVsTeamLogService;

    //全局属性=====================================================================================
    int ALL_TABLE_NUM_MAX = 10;
    int A_TABLE_NUM_MAX = 5;//A类用户桌子数量，必须比总量小

    List<TeamUser> online_user_list = new ArrayList<>();
    TeamTable[] table_arr = new TeamTable[ALL_TABLE_NUM_MAX];

    //添加在线玩家
    public void addPlayer(String openId,int tableId,int sitId,TeamWebSock tws) throws IOException {
        // 判断openId是否合法
        com.wangchen.entity.User member = userService.getUserByOpenId(openId);
        if (member == null) {
            tws.onClose();
            return;
        }

        // 判断用户是否已经在在线用户列表里
        if(online_user_list!=null && online_user_list.size()>0){
            for(TeamUser item : online_user_list){
                System.out.println("判断用户是否已经在在线用户列表里 openId:"+openId+" item.getOpenId:"+item.getOpenId());
                if(openId.equals(item.getOpenId())){
                    tws.onClose();
                    return;
                }
            }
        }

        //初始化用户
        TeamUser new_user = new TeamUser();
        new_user.setOpenId(openId);
        new_user.setType(member.getType());
        new_user.setName(member.getName());
        new_user.setAvatar(member.getAvatar());
        new_user.setOpenId(member.getOpenId());
        UserHonor userHonor = userHonorService.getLastHonor(new_user.getOpenId());
        new_user.setHonorId(null == userHonor?0:userHonor.getHonorId());
        new_user.setHonorName(null == userHonor?"未获段位":userHonor.getHonorName());
        UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",member.getOpenId()));
        new_user.setLevel(null == userLevel?0:userLevel.getLevelId()-1);
        List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id",openId).orderByDesc("create_time"));
        if(CollectionUtils.isEmpty(userAchievementList)){
            new_user.setAchievementName("未获成就");
        }else{
            new_user.setAchievementName(userAchievementList.get(0).getAchievementName());
        }
        List<UserTeamVsTeamLog> userTeamVsTeamLogList = userTeamVsTeamLogService.list(new QueryWrapper<UserTeamVsTeamLog>()
                .eq("open_id",openId).eq("create_date",Constants.SDF_YYYY_MM_DD.format(new Date())));
        if(CollectionUtils.isNotEmpty(userTeamVsTeamLogList)){
            new_user.setTeamGameCountToday(userTeamVsTeamLogList.size());
        }else{
            new_user.setTeamGameCountToday(0);
        }
        //保存该用户的连接线程句柄
        new_user.setTws(tws);

        //添加用户到列表
        online_user_list.add(new_user);

        //广播 p_count
        JSONObject data_obj = new JSONObject(); data_obj.put("num",online_user_list.size());
        JSONObject obj = new JSONObject(); obj.put("cmd","p_count"); obj.put("data",data_obj);
        broadCastMessage(obj,null);

        //发送大厅状态,包括桌子种类 1A 2BC
        List<JSONObject> data_list = new ArrayList<>();
        for(int i = 0; i<ALL_TABLE_NUM_MAX; i++) {
            data_obj = new JSONObject(); data_obj.put("table_id",i);
            data_obj.put("state",table_arr[i].getState());
            data_obj.put("type",(i<A_TABLE_NUM_MAX)?1:2);
            data_list.add(data_obj);
        }
        obj = new JSONObject(); obj.put("cmd","hall_state_list"); obj.put("data",data_list);
        tws.sendMessage(obj);

        //获得大厅玩家状况
        data_list = new ArrayList<>();
        for(int i = 0; i<ALL_TABLE_NUM_MAX; i++) {
            for(int j = 0; j<6; j++) {
                if(table_arr[i].getUser_arr()[j]==null){
                    continue;
                }
                data_obj = new JSONObject(); data_obj.put("table_id",i); data_obj.put("sit_id",j);
                data_obj.put("user",table_arr[i].getUser_arr()[j]);
                data_list.add(data_obj);
            }
        }
        obj = new JSONObject(); obj.put("cmd","hall_player_list"); obj.put("data",data_list);
        tws.sendMessage(obj);

        //处理受邀到大厅的入座 - onOpen()带tableId、sitId参数
        if(tableId < 0 || ALL_TABLE_NUM_MAX < tableId){return;}
        //判断桌子状态
        if(table_arr[tableId].getState()==1){
            data_obj = new JSONObject(); data_obj.put("msg","邀请你的人已经开战了");
            obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
            return;
        }
        //判断游玩场次
        if(new_user.getTeamGameCountToday()>0){
            data_obj = new JSONObject(); data_obj.put("msg","今天已经进行过了"+new_user.getTeamGameCountToday()+"次团队赛了");
            obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
            return;
        }
        boolean am_i_sitdown = false;
        if(sitId<3){//优先邀请到蓝队
            for(int i=0;i<6;i++){
                if(table_arr[tableId].getUser_arr()[i] == null){
                    stand(openId);
                    //坐下
                    table_arr[tableId].getUser_arr()[i] = new_user;
                    //广播 p_sit
                    data_obj = new JSONObject(); data_obj.put("table_id",tableId);data_obj.put("sit_id",i);
                    data_obj.put("user",new_user);
                    obj = new JSONObject(); obj.put("cmd","p_sit"); obj.put("data",data_obj);
                    broadCastMessage(obj,null);
                    am_i_sitdown = true;
                    break;
                }
            }
        }else{//优先邀请到红队
            for(int i=5;i>=0;i--){
                if(table_arr[tableId].getUser_arr()[i] == null){
                    stand(openId);
                    //坐下
                    table_arr[tableId].getUser_arr()[i] = new_user;
                    //广播 p_sit
                    data_obj = new JSONObject(); data_obj.put("table_id",tableId);data_obj.put("sit_id",i);
                    data_obj.put("user",new_user);
                    obj = new JSONObject(); obj.put("cmd","p_sit"); obj.put("data",data_obj);
                    broadCastMessage(obj,null);
                    am_i_sitdown = true;
                    break;
                }
            }
        }
        if(!am_i_sitdown){
            data_obj = new JSONObject(); data_obj.put("msg","邀请你的人已经满桌了");
            obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
        }

    }

    //删除在线玩家
    public void deletePlayer(String openId) throws IOException {
        //删除用户列表
        if(online_user_list!=null && online_user_list.size()>0){
            Iterator<TeamUser> iterator = online_user_list.iterator();
            while (iterator.hasNext()) {
                TeamUser next = iterator.next();
                if(next.getOpenId().equals(openId)){
                    iterator.remove();
                }
            }
        }

        //广播 p_count
        JSONObject data_obj = new JSONObject(); data_obj.put("num",online_user_list.size());
        JSONObject obj = new JSONObject(); obj.put("cmd","p_count"); obj.put("data",data_obj);
        broadCastMessage(obj,null);

        //处理玩家退出table的游戏逻辑
        for(int i = 0; i<ALL_TABLE_NUM_MAX; i++) {
            for(int j = 0; j<6; j++) {
                if(table_arr[i].getUser_arr()[j]==null){
                    continue;
                }
                if(table_arr[i].getUser_arr()[j].getOpenId().equals(openId)){
                    if(table_arr[i].getState()==1){
                        table_arr[i].playerQuit(openId,j);
                    }
                }
            }
        }

        //起立
        stand(openId);
    }

    //玩家聊天
    public void chat(String openId,String msg) throws IOException {
        //从列表获取用户
        TeamUser user = null;
        for(TeamUser item : online_user_list){
            if(openId.equals(item.getOpenId())){
                user = item;
                break;
            }
        }
        //openId不在用户列表里
        if(user==null){
            return;
        }
        for(int i = 0; i<ALL_TABLE_NUM_MAX; i++) {
            for(int j = 0; j<6; j++) {
                if(table_arr[i].getUser_arr()[j]==null){
                    continue;
                }
                if(table_arr[i].getUser_arr()[j].getOpenId().equals(openId)){
                    table_arr[i].getUser_arr()[j] = null;
                    //广播 chat
                    JSONObject data_obj = new JSONObject(); data_obj.put("msg",msg);
                    data_obj.put("user",user);
                    JSONObject obj = new JSONObject(); obj.put("cmd","chat"); obj.put("data",data_obj);
                    broadCastMessage(obj,null);
                }
            }
        }
    }

    //玩家入座
    public void sit(String openId,int tableId,int sitId,TeamWebSock tws) throws IOException {

        //桌号越值
        if(tableId < 0 || ALL_TABLE_NUM_MAX < tableId){return;}
        //座位号越值
        if(sitId < 0 || 5 < sitId){return;}

        //从列表获取用户
        TeamUser user = null;
        for(TeamUser item : online_user_list){
            if(openId.equals(item.getOpenId())){
                user = item;
                break;
            }
        }
        //openId不在用户列表里
        if(user==null){
            tws.onClose();
            return;
        }

        //判断桌子状态
        if(table_arr[tableId].getState()==1){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","该桌已经开战了");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
            return;
        }

        //判断该座位是否有人
        if(table_arr[tableId].getUser_arr()[sitId] != null){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","该桌已经有人了");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
            return;
        }

        //起立 - 删除大厅座位列表中的该用户(如果入座的话)
        stand(openId);

        //判断游玩场次
        if(user.getTeamGameCountToday()>0){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","今天已经进行过了"+user.getTeamGameCountToday()+"次团队赛了");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
            return;
        }

        //坐下
        table_arr[tableId].getUser_arr()[sitId] = user;
        //广播 p_sit
        JSONObject data_obj = new JSONObject(); data_obj.put("table_id",tableId);data_obj.put("sit_id",sitId);
        data_obj.put("user",user);
        JSONObject obj = new JSONObject(); obj.put("cmd","p_sit"); obj.put("data",data_obj);
        broadCastMessage(obj,null);
    }

    //玩家起立
    public void stand(String openId) throws IOException {
        for(int i = 0; i<ALL_TABLE_NUM_MAX; i++) {
            for(int j = 0; j<6; j++) {
                if(table_arr[i].getUser_arr()[j]==null){
                    continue;
                }
                if(table_arr[i].getUser_arr()[j].getOpenId().equals(openId)){
                    table_arr[i].getUser_arr()[j] = null;
                    //广播 p_stand
                    JSONObject data_obj = new JSONObject(); data_obj.put("table_id",i); data_obj.put("sit_id",j);
                    data_obj.put("open_id",openId);
                    JSONObject obj = new JSONObject(); obj.put("cmd","p_stand"); obj.put("data",data_obj);
                    broadCastMessage(obj,null);
                }
            }
        }
    }

    //队长踢人
    public void kick(String openId,int tableId,int sitId,TeamWebSock tws) throws IOException {
        if(table_arr[tableId].getUser_arr()[0]==null){
            return;
        }
        if(!table_arr[tableId].getUser_arr()[0].getOpenId().equals(openId)){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","你不是房主");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
            return;
        }
        if(table_arr[tableId].getUser_arr()[sitId]==null){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","该座位没人");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
        }else{
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","抱歉，您被房主请出了该桌");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            table_arr[tableId].getUser_arr()[sitId].getTws().sendMessage(obj);//发送error msg

            //广播 p_stand
            data_obj = new JSONObject(); data_obj.put("table_id",tableId);data_obj.put("sit_id",sitId);
            data_obj.put("open_id",table_arr[tableId].getUser_arr()[sitId].getOpenId());
            obj = new JSONObject(); obj.put("cmd","p_stand"); obj.put("data",data_obj);
            broadCastMessage(obj,null);

            //清空table中的目标用户
            table_arr[tableId].getUser_arr()[sitId]=null;
        }
    }

    //队长开始游戏
    public void start(String openId,TeamWebSock tws) throws IOException {
        int table_id = -1;
        for(TeamTable item : table_arr){
            if(item.getUser_arr()[0]!=null){
                if(item.getUser_arr()[0].getOpenId().equals(openId)){
                    table_id = item.getTable_id();
                    item.start();
                    break;
                }
            }
        }
        if(table_id==-1){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","房间不存在或你不是房主");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
        }
    }

    //玩家作答
    public void select(String openId, int topicNum, int sNum, TeamWebSock tws) throws IOException {
        int table_id = -1;
        int sit_id = -1;
        for(TeamTable item : table_arr){
            for(int i=0;i<6;i++){
                if(item.getUser_arr()[i]!=null){
                    if(item.getUser_arr()[i].getOpenId().equals(openId)){
                        table_id = item.getTable_id();
                        sit_id = i;
                        item.select(topicNum,sNum,sit_id,false);
                        break;
                    }
                }
            }
        }
        if(table_id == -1){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","房间不存在");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
        }
    }

    //超时
    public void timeOut(String openId, int topicNum, TeamWebSock tws) throws IOException {
        int table_id = -1;
        int sit_id = -1;
        for(TeamTable item : table_arr){
            for(int i=0;i<6;i++){
                if(item.getUser_arr()[i]!=null){
                    if(item.getUser_arr()[i].getOpenId().equals(openId)){
                        table_id = item.getTable_id();
                        sit_id = i;
                        item.timeOut(topicNum,sit_id);
                        break;
                    }
                }
            }
        }
        if(table_id == -1){
            JSONObject data_obj = new JSONObject(); data_obj.put("msg","房间不存在");
            JSONObject obj = new JSONObject(); obj.put("cmd","error"); obj.put("data",data_obj);
            tws.sendMessage(obj);
        }
    }

    //广播数据 openId有值包括自己 为null不包括自己
    public void broadCastMessage(JSONObject object,String openId){
        if(online_user_list!=null && online_user_list.size()>0) {
            for (TeamUser item : online_user_list) {
                if (!item.getOpenId().equals(openId)) {
                    item.getTws().sendMessage(object);
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        instance = this;
        instance.userService = this.userService;
        instance.userHonorService = this.userHonorService;
        instance.userLevelService = this.userLevelService;
        instance.userAchievementService = this.userAchievementService;

        for (int i=0; i<ALL_TABLE_NUM_MAX; i++){
            table_arr[i] = new TeamTable(i);
        }
    }
}

