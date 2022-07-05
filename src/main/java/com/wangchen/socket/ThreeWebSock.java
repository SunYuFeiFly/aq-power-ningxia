package com.wangchen.socket;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.*;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.entity.User;
import com.wangchen.service.*;
import com.wangchen.vo.ThreeTeamTopicVo;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: 好友约战
 * @Author: chengzhang
 * @Date: 2019/12/3 15:00
 *  ,,,
 * @Version: 1.0
 */
@Slf4j
@ServerEndpoint("/threeWebsocket/{openId}/{roomId}")
@Component
public class ThreeWebSock {
    private static int onlineCount = 0;
    private static CopyOnWriteArrayList<ThreeWebSock> webSocketSet=new CopyOnWriteArrayList<ThreeWebSock>();

    private Session session;

    //是否已经开始游戏
    private boolean isGame;

    //本人和房间号以及 本局所有队友
    private ThreeResultWebSocket threeResultWebSocket;

    private List<ThreeTeamTopicVo> answerResponse;//题目信息

    private boolean isReset = false;// 是否重置用户答题信息 false：否 true:是

    private boolean isTimeOut = false;//是否接受到了超时



    private static UserService userService;
    @Autowired
    public void setUserService(UserService userService){
        ThreeWebSock.userService = userService;
    }

    private static UserHonorService userHonorService;
    @Autowired
    public void setUserHonorService(UserHonorService userHonorService){
        ThreeWebSock.userHonorService = userHonorService;
    }

    private static ThreeRoomService threeRoomService;
    @Autowired
    public void setThreeRoomService(ThreeRoomService threeRoomService){
        ThreeWebSock.threeRoomService = threeRoomService;
    }

    private static UserThreeTeamLogService userThreeTeamLogService;
    @Autowired
    public void setUserThreeTeamLogService(UserThreeTeamLogService userThreeTeamLogService){
        ThreeWebSock.userThreeTeamLogService = userThreeTeamLogService;
    }

    private static BranchTopicService branchTopicService;
    @Autowired
    public void setBranchTopicService(BranchTopicService branchTopicService){
        ThreeWebSock.branchTopicService = branchTopicService;
    }

    private static BranchOptionService branchOptionService;
    @Autowired
    public void setBranchOptionService(BranchOptionService branchOptionService){
        ThreeWebSock.branchOptionService = branchOptionService;
    }

    private static UserLevelService userLevelService;
    @Autowired
    public void setUserLevelService(UserLevelService userLevelService){
        ThreeWebSock.userLevelService = userLevelService;
    }



    @OnOpen
    public void onOpen(@PathParam("openId") String openId, @PathParam("roomId") long roomId, Session session){
        try {
            log.info("three connect onOpen:openId:{} ,roomId:{}",openId,roomId);

            if(StringUtils.isEmpty(openId)){
                log.error("openId");
                sendMessage(session,JSONObject.toJSONString(Result.newFail(BusinessErrorMsg.OPEN_ID_IS_NULL)));
                return;
            }

            User user = userService.getUserByOpenId(openId);
            if(null == user){
                log.error("通过openId 未找到用户信息");
                sendMessage(session,JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.USER_TOKEN_IS_NULL.getMsg())));
                return;
            }

            this.session =session;
            //房间创建人
            if(0 == roomId){

                //查询房间里是否已经 有本次进来的人
                for(ThreeWebSock threeWebSock : webSocketSet){
                    if(threeWebSock.threeResultWebSocket.getUserResult().equals(openId)){
                        if(!threeWebSock.isGame){
                            threeWebSock.onClose();
                            break;
                        }
                    }
                }

                // 查看最大房间号
                Long maxRoomNo = threeRoomService.selectMaxRoomNo();
                // 生成房间
                ThreeRoom room = new ThreeRoom();
                room.setRoomNo(maxRoomNo == null ? 1 : (maxRoomNo + 1));
                room.setCreateDate(new Date());
                room.setIsOpen(0);
                room.setScore(0);//总分为0
                threeRoomService.save(room);

                ThreeUser threeUser =new ThreeUser();
                threeUser.setAvatar(user.getAvatar());
                threeUser.setNickName(user.getName());
                threeUser.setOpenId(user.getOpenId());

                UserHonor userHonor = userHonorService.getLastHonor(user.getOpenId());
                threeUser.setHonorName(null == userHonor?"暂未获取段位":userHonor.getHonorName());
                UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",user.getOpenId()));
                threeUser.setLevel(null == userLevel?0:userLevel.getLevelId()-1);

                ThreeUserResult userResult = new ThreeUserResult();
                userResult.setIsHouseOwner(1);
                userResult.setThreeUser(threeUser);
                userResult.setActive(true);

                ThreeResultWebSocket resultWebSocket = new ThreeResultWebSocket();
                resultWebSocket.setRoom(room.getRoomNo());
                resultWebSocket.setIsHouseOwner(1);
                resultWebSocket.setGameState(0);
                resultWebSocket.setUserList(new ArrayList<>());
                resultWebSocket.getUserList().add(userResult);
                this.threeResultWebSocket = resultWebSocket.clone();
                this.threeResultWebSocket.setUserResult(userResult);
                this.isGame = false;//是否游戏中

                webSocketSet.add(this);//加入set中
                addOnlineCount();
                this.sendMessage(this.session,toJSONString(this.threeResultWebSocket));
                log.debug("-+--------------创建房间发送信息: {}",toJSONString(this.threeResultWebSocket));
            }else{
                //获取到房间里所有的人
                List<ThreeWebSock> readys = new ArrayList<ThreeWebSock>();
                for(ThreeWebSock webSock : webSocketSet){
                    //获取房间中的所有用户
                    if(roomId == webSock.threeResultWebSocket.getRoom()){
                        readys.add(webSock);
                    }
                }

                //查询房间里是否已经 有本次进来的人
                for(ThreeWebSock threeWebSock : webSocketSet){
                    if(threeWebSock.threeResultWebSocket.getUserResult().equals(openId)){
                        threeWebSock.onClose();
                        break;
                    }
                }

                //如果房间里没人了
                if(readys.size() <= 0){
                    this.sendMessage(session,JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.ROOM_IS_NULL.getCode(),BussinessErrorMsg.ROOM_IS_NULL.getMsg())));
//                    this.threeResultWebSocket.setCode(BussinessErrorMsg.ROOM_IS_NULL.getCode());
//                    this.sendMessage(session,toJSONString(this.threeResultWebSocket));
//                    this.threeResultWebSocket.setCode(null);
                    return;
                }

                //如果房间里的玩家有5个以上，那就满员了   因为一个房间最多6个人
                if(readys.size()>2){
                    log.error("房间已经满员");
                    this.sendMessage(session,JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.ROOM_YI_JING_MAN_YUAN.getCode(),BussinessErrorMsg.ROOM_YI_JING_MAN_YUAN.getMsg())));
//                    this.threeResultWebSocket.setCode(BussinessErrorMsg.ROOM_YI_JING_MAN_YUAN.getCode());
//                    this.sendMessage(session,toJSONString(this.threeResultWebSocket));
//                    this.threeResultWebSocket.setCode(null);
                    return;
                }

//                int i = -1;
//                for(ThreeWebSock item:readys){
//                    if(item.threeResultWebSocket.getUserResult().getThreeUser().getOpenId().equals(openId)){
//                        for(ThreeUserResult threeUserResult : item.threeResultWebSocket.getUserList()){
//                            if(threeUserResult.getThreeUser().getOpenId().equals(openId)){
////                                item.threeResultWebSocket.getUserList().remove(threeUserResult);
//                                item.onClose();
//                                readys.remove(i);
//                                break;
//                            }
//                        }
////                        item.sendMessage(session,JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.ROOM_YI_JING_ROOM_LI.getCode(),BussinessErrorMsg.ROOM_YI_JING_ROOM_LI.getMsg())));
////                        return;
//                    }
//                    i++;
//                }

                ThreeUser threeUser =new ThreeUser();
                threeUser.setAvatar(user.getAvatar());
                threeUser.setNickName(user.getName());
                threeUser.setOpenId(user.getOpenId());
                UserHonor userHonor = userHonorService.getLastHonor(user.getOpenId());
                threeUser.setHonorName(null == userHonor?"暂未获取段位":userHonor.getHonorName());
                UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",user.getOpenId()));
                threeUser.setLevel(null == userLevel?0:userLevel.getLevelId()-1);

                ThreeUserResult userResult = new ThreeUserResult();
                userResult.setIsHouseOwner(0);
                userResult.setThreeUser(threeUser);
                userResult.setActive(true);

                ThreeResultWebSocket resultWebSocket = new ThreeResultWebSocket();
                resultWebSocket.setRoom(roomId);
                resultWebSocket.setIsHouseOwner(0);
                resultWebSocket.setGameState(0);
                resultWebSocket.setUserList(new ArrayList<>());
                this.threeResultWebSocket = resultWebSocket.clone();
                this.threeResultWebSocket.setUserResult(userResult);
                this.isGame = false;//是否游戏中

                for(ThreeWebSock item : readys){
                    this.threeResultWebSocket.getUserList().add(item.threeResultWebSocket.getUserResult());
                    item.threeResultWebSocket.getUserList().add(this.threeResultWebSocket.getUserResult());
                    item.sendMessage(item.session,toJSONString(item.threeResultWebSocket));
                }
                this.threeResultWebSocket.getUserList().add(this.threeResultWebSocket.getUserResult());
                webSocketSet.add(this);
                this.sendMessage(this.session,toJSONString(this.threeResultWebSocket));
            }
        }catch (Exception e){
            log.error("onOpen error:{}", e);
        }
    }



    @OnClose
    public void onClose(){
        log.info("{}交互onClose", this.threeResultWebSocket.getUserResult().getThreeUser().getNickName());
        subOnlineCount();
        synchronized (this){

            webSocketSet.remove(this);
            // 告诉同房间所有人，该用户已经退出游戏了
            List<ThreeWebSock> webSocks = getAllWebSockDelMe(this);
            int num=0;
            String nowFanZhu = null;
            for(ThreeWebSock item:webSocks){
                //先把所有用户 队友中当前退出人删除掉
                if(this.threeResultWebSocket.getIsHouseOwner() == 1){
                    if(num == 0){
                        item.threeResultWebSocket.setIsHouseOwner(1);
                        nowFanZhu = item.threeResultWebSocket.getUserResult().getThreeUser().getOpenId();
                        num++;
                    }
                }

                if(isGame){
                    List<ThreeUserResult> userList= item.threeResultWebSocket.getUserList();
                    for(int i=0; i<userList.size();i++){
                        if(userList.get(i).getIsNowAnswer() == 1){
                            userList.get(i).setIs_answer(2);
                            userList.get(i).setSelectAnswerOpt("timeOut");
                            userList.get(i).setSelectAnswerOpt2("timeOut");
                        }
//                    if(userList.get(i).getThreeUser().getOpenId().equals(this.threeResultWebSocket.getUserResult().getThreeUser().getOpenId())){
//                        userList.remove(i);
//                    }

                        if(StringUtils.isNotBlank(nowFanZhu)){
                            if(nowFanZhu.equals(userList.get(i).getThreeUser().getOpenId())){
                                userList.get(i).setIsHouseOwner(1);
                            }
                        }
                    }
                }else{
                    List<ThreeUserResult> userList= item.threeResultWebSocket.getUserList();
                    for(int i=0; i<userList.size();i++){
                        if(userList.get(i).getThreeUser().getOpenId().equals(this.threeResultWebSocket.getUserResult().getThreeUser().getOpenId())){
                            userList.remove(i);
                        }

                        if(StringUtils.isNotBlank(nowFanZhu)){
                            if(nowFanZhu.equals(userList.get(i).getThreeUser().getOpenId())){
                                userList.get(i).setIsHouseOwner(1);
                            }
                        }
                    }
                }


                try {
                    if(isGame){
                        List<ThreeRoom> threeRoomList = threeRoomService.list(new QueryWrapper<ThreeRoom>()
                                .eq("create_date",Constants.SDF_YYYY_MM_DD.format(new Date())).eq("is_open",1)
                                .orderByDesc("score").orderByAsc("end_time"));
                        int teamRankNo = 0;
                        if(CollectionUtils.isEmpty(threeRoomList) || threeRoomList.size() == 1){
                            teamRankNo = 1;
                        }else{
                            List<Integer> scoreList = new ArrayList<Integer>();
                            for(ThreeRoom threeRoom : threeRoomList){
                                if(!scoreList.contains(threeRoom.getScore())){
                                    scoreList.add(threeRoom.getScore());
                                }
                            }
                            teamRankNo = scoreList.indexOf(item.threeResultWebSocket.getAllScore())+1;
                        }
                        item.threeResultWebSocket.setTeamRank(teamRankNo);
                        item.threeResultWebSocket.setCode(BussinessErrorMsg.PLAYER_LEAVE.getCode());
                        item.threeResultWebSocket.setGameState(3);
                        item.sendMessage(item.session, toJSONString(item.threeResultWebSocket));
                        item.threeResultWebSocket.setCode(null);
//                        return;
                    }else{
                        //告诉房间里所有人有人退出了
//                        item.threeResultWebSocket.setCode(BussinessErrorMsg.PLAYER_LEAVE.getCode());
                        item.sendMessage(item.session, toJSONString(item.threeResultWebSocket));
//                        item.threeResultWebSocket.setCode(null);
//                        return;
                    }
                }catch (Exception e){
                    log.error("sendMessage error:{}", e);
                }
            }
            log.info("有一连接关闭！当前在线人数为,count:{}", getOnlineCount());
            return;
        }
    }


    @OnMessage
    public synchronized void onMessage(String message, Session session){
        try {
            log.info("{}交互onMessage", this.threeResultWebSocket.getUserResult().getThreeUser().getNickName());
            // 心跳 pong
            // 聊天 chat
            // 答题 answer
            if (!JSONObject.isValid(message)){
                return;
            }

            JSONObject json = JSONObject.parseObject(message);
            String type = json.getString("type");

            if("pong".equals(type)){
                this.sendMessage(session,JSONObject.toJSONString(
                        Result.newFail(BussinessErrorMsg.PONG_ONE.getCode(),BussinessErrorMsg.PONG_ONE.getMsg())));
                return;
            }

            //点击开始
            if("start".equals(type)){
                boolean flag = true;
                List<ThreeUserResult> userResultList = this.threeResultWebSocket.getUserList();

                //一个人是不能开始游戏的
                if(userResultList.size()<=1){
                    this.threeResultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_ONE.getCode());
                    this.sendMessage(this.session,toJSONString(this.threeResultWebSocket));
                    this.threeResultWebSocket.setCode(null);
                    return;
                }

                //一个人是不能开始游戏的
                if(userResultList.size()!= 3){
                    this.threeResultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_THREE.getCode());
                    this.sendMessage(this.session,toJSONString(this.threeResultWebSocket));
                    this.threeResultWebSocket.setCode(null);
                    return;
                }

                for(ThreeUserResult userResult:userResultList){
                    UserThreeTeamLog userThreeTeamLog = userThreeTeamLogService.getOne(new QueryWrapper<UserThreeTeamLog>()
                            .eq("open_id",userResult.getThreeUser().getOpenId())
                            .eq("create_date", Constants.SDF_YYYY_MM_DD.format(new Date())));
                    //确定一下 是否有玩过游戏或者队友是否玩过游戏
                    if(null != userThreeTeamLog){
                        flag = false;
                        break;
                    }
                }

                if(!flag){//不能开， 可能是跟某些人匹配过了
                    this.threeResultWebSocket.setCode(BussinessErrorMsg.PVP_NOT_READY.getCode());
                    this.sendMessage(this.session,toJSONString(this.threeResultWebSocket));
                    this.threeResultWebSocket.setCode(null);
                    return;
                }else{
                    List<ThreeWebSock> webSocks = getAllWebSock(this);
                    //获取题目信息
                    List<BranchTopic> topicListTopic = branchTopicService.listTopicRandom(10);
                    List<ThreeTeamTopicVo> topicList = new ArrayList<ThreeTeamTopicVo>();

                    for(int i=1; i<=topicListTopic.size();i++){
                        ThreeTeamTopicVo threeTeamTopicVo =new ThreeTeamTopicVo();
                        BeanUtils.copyProperties(topicListTopic.get(i-1),threeTeamTopicVo);
                        threeTeamTopicVo.setRankNo(i);
                        threeTeamTopicVo.setOptionList(branchOptionService.list(new QueryWrapper<BranchOption>()
                                .eq("topic_id",threeTeamTopicVo.getId())));//答案信息
                        topicList.add(threeTeamTopicVo);
                    }

                    webSocks.get(0).threeResultWebSocket.setIsNowAnswer(1);//房主为第一个答题的
                    webSocks.get(0).threeResultWebSocket.getUserResult().setIsNowAnswer(1);//房主为第一个答题的
                    for(ThreeWebSock item : webSocks){
                        item.answerResponse=topicList;
                        item.isGame=true;
                        item.threeResultWebSocket.setGameState(1);

                        List<ThreeUserResult> itemUserList = item.threeResultWebSocket.getUserList();
                        for(ThreeUserResult userResult : itemUserList){
                            if(userResult.getThreeUser().getOpenId().equals(webSocks.get(0).threeResultWebSocket.getUserResult().getThreeUser().getOpenId())){
                                userResult.setIsNowAnswer(1);
                            }else{
                                userResult.setIsNowAnswer(0);
                            }
                        }
                        item.sendMessage(item.session, JSONObject.toJSONString(Result.newSuccess(BusinessErrorMsg.ANSWER, item.answerResponse)));
                        item.sendMessage(item.session,toJSONString(item.threeResultWebSocket));
                    }

                    ThreeRoom threeRoom = threeRoomService.getOne(new QueryWrapper<ThreeRoom>()
                            .eq("room_no",webSocks.get(0).threeResultWebSocket.getRoom()));
                    if(null != threeRoom){
                        threeRoom.setIsOpen(1);
                        threeRoom.setOpenTime(new Date());
                        threeRoomService.updateById(threeRoom);
                    }
                    //记录下本房间的三个人
                    for(ThreeUserResult threeUserResult : this.threeResultWebSocket.getUserList()){
                        UserThreeTeamLog userThreeTeamLog = new UserThreeTeamLog();
                        userThreeTeamLog.setOpenId(threeUserResult.getThreeUser().getOpenId());
                        userThreeTeamLog.setRoomId(threeRoom.getId());
                        userThreeTeamLog.setCreateDate(new Date());
                        userThreeTeamLogService.save(userThreeTeamLog);
                    }
                    return;
                }
            }

            //答题
            if("answer".equals(type)){
                String answer1 = json.getString("answer1");//玩家回答的答案
                String answer2 = json.getString("answer2");//玩家回答的答案

                Integer answerId = json.getInteger("answerId");//题目id

                Integer topicId = json.getInteger("topicId");//题目id
                // 答题时间
                String timeStr = json.getString("time");

                if(this.isTimeOut){
                    log.debug("-------------------阻拦住了timeOut: 发送人: {}",this.threeResultWebSocket.getUserResult().getThreeUser().getNickName());
                    return;
                }

                //TODO 题目信息
                ThreeTeamTopicVo pacmanDrawTopicVo = null;
                //循环所有题目
                for(ThreeTeamTopicVo topic:this.answerResponse){
                    //根据传过来的id获取到题目信息
                    if(topic.getId().intValue() == topicId.intValue()){
                        pacmanDrawTopicVo = topic;
                        break;
                    }
                }

                if(null == pacmanDrawTopicVo){
                    log.error("------------------团队赛答题接收到题目id不存在在本次10道题中、传来的topicId为:{}",topicId);
                    this.threeResultWebSocket.setCode(BussinessErrorMsg.BUSINESS_ERROR.getCode());
                    this.sendMessage(this.session,toJSONString(this.threeResultWebSocket));
                    this.threeResultWebSocket.setCode(null);
                    return;
                }

                boolean isAnswerTrue = false;
                boolean isTimeOutTrue =false;
                if(!"-1".equals(timeStr)){
                    //选择题判断答题对错
                    if(0 == pacmanDrawTopicVo.getTopicType().intValue()){
                        //遍历答案集合  用答案正确id和答案集合做对比
                        if(pacmanDrawTopicVo.getCorrectOptionId().intValue() == answerId.intValue()){
                            isAnswerTrue = true;
                        }

                    }else if(1 == pacmanDrawTopicVo.getTopicType().intValue()){//填空题

                        if(pacmanDrawTopicVo.getOptionList().size() == 1){
                            if(answer1.equals(pacmanDrawTopicVo.getOptionList().get(0).getContent())){
                                isAnswerTrue = true;
                            }
                        }else{
                            if(answer1.equals(pacmanDrawTopicVo.getOptionList().get(0).getContent())
                                    && answer2.equals(pacmanDrawTopicVo.getOptionList().get(1).getContent())){
                                isAnswerTrue = true;
                            }
                        }
                    }else if(2 == pacmanDrawTopicVo.getTopicType().intValue()){//判断题
                        if(answer1.equals(pacmanDrawTopicVo.getOptionList().get(0).getContent())){
                            isAnswerTrue = true;
                        }
                    }
                }else{
                    isTimeOutTrue = true;
                }

                int gameStatus = 2;//如果是最后一题 那就结束

                if(topicId.intValue() == this.answerResponse.get(this.answerResponse.size() - 1).getId()) {
                    log.info("进入timeOut ----------返回游戏状态结束------------------------------------------");
                    gameStatus = 3;//2是继续进行 3是题目全部答完
                }

                //判断答案是否正确
                if(isAnswerTrue){
                    //如果已经答对了，显示不能重复答题
                    if(this.threeResultWebSocket.getUserResult().getIs_answer() == 1 || this.threeResultWebSocket.getUserResult().getIs_answer() == 2){
                        this.threeResultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_HAS_ANSWER.getCode());
                        this.sendMessage(this.session,toJSONString(this.threeResultWebSocket));
                        this.threeResultWebSocket.setCode(null);
                        return;
                    }

                    //答题正确
                    this.threeResultWebSocket.getUserResult().setIs_answer(1);

                    List<ThreeWebSock> threeWebSockList = ThreeWebSock.getAllWebSock(this);
                    //本次团队排名
                    boolean isTeamRankFlag = true;
                    int teamRankNo = 0;
                    //把当前答题成功的人的情况告诉 别的玩家
                    for(ThreeWebSock item : threeWebSockList){
                        item.threeResultWebSocket.setCode(null);
                        item.isReset = false;
                        item.isTimeOut = isTimeOutTrue;
                        item.threeResultWebSocket.setAllScore(item.threeResultWebSocket.getAllScore()+10);//总分数
                        //同步 另外玩家里自己的状态
                        List<ThreeUserResult> userResultList = item.threeResultWebSocket.getUserList();
                        for(int i=0;i<userResultList.size();i++){
                            if(this.threeResultWebSocket.getUserResult().getThreeUser().getOpenId().equals(userResultList.get(i).getThreeUser().getOpenId())){
                                userResultList.get(i).setIs_answer(1);//答题正确
                                userResultList.get(i).setSelectAnswer(answerId);
                                userResultList.get(i).setSelectAnswerOpt(answer1);
                                userResultList.get(i).setSelectAnswerOpt2(answer2);
                            }
                        }
                        item.threeResultWebSocket.setGameState(gameStatus);
                        if(gameStatus == 3){
                            //如果结束 需要设置一下未开始游戏 因为前端弹结算页面了，所以不需要在结束了，因为退出的时候会判断一下
                            //如果退出的时候gameStatus还是3的话 就结算，但实际是正常结束了(详情可以看 onclose方法)
                            item.isGame = false;

                            if(isTeamRankFlag){
                                isTeamRankFlag =false;
                                List<ThreeRoom> threeRoomList = threeRoomService.list(new QueryWrapper<ThreeRoom>()
                                        .eq("create_date",Constants.SDF_YYYY_MM_DD.format(new Date())).eq("is_open",1)
                                        .orderByDesc("score").orderByAsc("end_time"));
                                if(CollectionUtils.isEmpty(threeRoomList) || threeRoomList.size() == 1){
                                    teamRankNo = 1;
                                }else{
                                    List<Integer> scoreList = new ArrayList<Integer>();
                                    for(ThreeRoom threeRoom : threeRoomList){
                                        if(!scoreList.contains(threeRoom.getScore())){
                                            scoreList.add(threeRoom.getScore());
                                        }
                                    }
                                    teamRankNo = scoreList.indexOf(item.threeResultWebSocket.getAllScore())+1;
                                }
                            }
                            item.threeResultWebSocket.setTeamRank(teamRankNo);
                        }
                        item.sendMessage(item.session,toJSONString(item.threeResultWebSocket));
                    }

                    if(gameStatus == 3){
                        ThreeRoom threeRoom = threeRoomService.getOne(new QueryWrapper<ThreeRoom>()
                                .eq("room_no",this.threeResultWebSocket.getRoom()));
                        threeRoom.setScore(this.threeResultWebSocket.getAllScore());
                        threeRoom.setEndTime(new Date());
                        threeRoomService.updateById(threeRoom);
                    }

                }else{

                    this.threeResultWebSocket.getUserResult().setIs_answer(2);
                    //获取房间里所有的人(不包括自己)
                    List<ThreeWebSock> webs = getAllWebSock(this);
                    //把当前答题成功的人的情况告诉 别的玩家
                    //本次团队排名
                    boolean isTeamRankFlag = true;
                    int teamRankNo = 0;
                    for(ThreeWebSock item : webs){
                        item.isReset = false;
                        item.isTimeOut = isTimeOutTrue;
                        item.threeResultWebSocket.setCode(null);
                        //同步 另外玩家里自己的状态
                        List<ThreeUserResult> userResultList = item.threeResultWebSocket.getUserList();
                        for(int i=0;i<userResultList.size();i++){
                            if(this.threeResultWebSocket.getUserResult().getIsNowAnswer() == 1){
                                if(this.threeResultWebSocket.getUserResult().getThreeUser().getOpenId().equals(userResultList.get(i).getThreeUser().getOpenId())){
                                    userResultList.get(i).setIs_answer(2);
                                    userResultList.get(i).setSelectAnswer(answerId);
                                    if(!"-1".equals(timeStr)){
                                        userResultList.get(i).setSelectAnswerOpt(answer1);
                                        userResultList.get(i).setSelectAnswerOpt2(answer2);
                                    }else{
                                        userResultList.get(i).setSelectAnswerOpt("timeOut");
                                        userResultList.get(i).setSelectAnswerOpt2("timeOut");
                                    }
                                }
                            }else{
                                if(userResultList.get(i).getIsNowAnswer() == 1){
                                    userResultList.get(i).setIs_answer(2);
                                    userResultList.get(i).setSelectAnswer(answerId);
                                    if("-1".equals(timeStr)){
                                        userResultList.get(i).setSelectAnswerOpt("timeOut");
                                        userResultList.get(i).setSelectAnswerOpt2("timeOut");
                                    }
                                }
                            }
                        }
                        item.threeResultWebSocket.setGameState(gameStatus);
                        if(gameStatus == 3){
                            //如果结束 需要设置一下未开始游戏 因为前端弹结算页面了，所以不需要在结束了，因为退出的时候会判断一下
                            //如果退出的时候gameStatus还是3的话 就结算，但实际是正常结束了(详情可以看 onclose方法)
                            item.isGame = false;

                            if(isTeamRankFlag){
                                isTeamRankFlag =false;
                                List<ThreeRoom> threeRoomList = threeRoomService.list(new QueryWrapper<ThreeRoom>()
                                        .eq("create_date",Constants.SDF_YYYY_MM_DD.format(new Date())).eq("is_open",1)
                                        .orderByDesc("score").orderByAsc("end_time"));
                                if(CollectionUtils.isEmpty(threeRoomList) || threeRoomList.size() == 1){
                                    teamRankNo = 1;
                                }else{
                                    List<Integer> scoreList = new ArrayList<Integer>();
                                    for(ThreeRoom threeRoom : threeRoomList){
                                        if(!scoreList.contains(threeRoom.getScore())){
                                            scoreList.add(threeRoom.getScore());
                                        }
                                    }
                                    teamRankNo = scoreList.indexOf(item.threeResultWebSocket.getAllScore())+1;
                                }

                            }
                            item.threeResultWebSocket.setTeamRank(teamRankNo);
                        }
                        log.debug("-------------------发送了一次timeOut: 发送人: {}",this.threeResultWebSocket.getUserResult().getThreeUser().getNickName());
                        item.sendMessage(item.session,toJSONString(item.threeResultWebSocket));
                    }

                    if(gameStatus == 3){
                        ThreeRoom threeRoom = threeRoomService.getOne(new QueryWrapper<ThreeRoom>()
                                .eq("room_no",this.threeResultWebSocket.getRoom()));
                        threeRoom.setScore(this.threeResultWebSocket.getAllScore());
                        threeRoom.setEndTime(new Date());
                        threeRoomService.updateById(threeRoom);
                    }

                }
                return;
            }

            //下一题
            if("isNext".equals(type)){
                // 重置该房间所有用户数据
                // 如果已经重置则跳过
                synchronized (this){
                    if(this.isReset){
                        return;
                    }
                    List<ThreeWebSock> webSocks = getAllWebSock(this);

                    int temp= 0;
                    for (int i=0;i< webSocks.size();i++){
                        webSocks.get(i).isTimeOut = false;
                        if (webSocks.get(i).threeResultWebSocket == null){
                            continue;
                        }

                        if(webSocks.get(i).threeResultWebSocket.getIsNowAnswer() == 1){
                            temp = i;
                            webSocks.get(i).threeResultWebSocket.setIsNowAnswer(0);
                            webSocks.get(i).threeResultWebSocket.getUserResult().setIsNowAnswer(0);
                        }

                        for(int j=0;j<webSocks.get(i).threeResultWebSocket.getUserList().size();j++){
                            webSocks.get(i).threeResultWebSocket.getUserList().get(j).setSelectAnswer(null);
                            webSocks.get(i).threeResultWebSocket.getUserList().get(j).setSelectAnswerOpt(null);
                            webSocks.get(i).threeResultWebSocket.getUserList().get(j).setSelectAnswerOpt2(null);
                        }

                        webSocks.get(i).threeResultWebSocket.setGameState(1);
                        webSocks.get(i).threeResultWebSocket.getUserResult().setIs_answer(0);
                        webSocks.get(i).isReset = true;

                    }

                    //如果本次是最后一个人答题，那下一题就重新轮回 就是第1个人答
                    if(temp == 2){
                        temp = 0;
                        webSocks.get(temp).threeResultWebSocket.setIsNowAnswer(1);
                        webSocks.get(temp).threeResultWebSocket.getUserResult().setIsNowAnswer(1);
                    }else{
                        if(temp == 0 || temp == 1){
                            temp = temp+1;
                            webSocks.get(temp).threeResultWebSocket.setIsNowAnswer(1);
                            webSocks.get(temp).threeResultWebSocket.getUserResult().setIsNowAnswer(1);
                        }
                    }

                    String openId = webSocks.get(temp).threeResultWebSocket.getUserResult().getThreeUser().getOpenId();
                    for(ThreeWebSock item:webSocks){
                        for(ThreeUserResult itemUserResult:item.threeResultWebSocket.getUserList()){
                            if(openId.equals(itemUserResult.getThreeUser().getOpenId())){
                                itemUserResult.setIsNowAnswer(1);
                            }else{
                                itemUserResult.setIsNowAnswer(0);
                            }
                        }
                    }

                    for(ThreeWebSock item: webSocks){
                        item.sendMessage(item.session,toJSONString(item.threeResultWebSocket));
                    }
                }
                return;
            }

            //下一题
            if("tiren".equals(type)){
                synchronized (this){
                    if(this.isGame){
                        return;
                    }
                    String tiOpenId = json.getString("tiOpenId");//玩家回答的答案

                    List<ThreeWebSock> webSocks = getAllWebSock(this);

                    for(ThreeWebSock item : webSocks){
                        if(item.threeResultWebSocket.getUserResult().getThreeUser().getOpenId().equals(tiOpenId)){
                            item.threeResultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_OUT_ROOM.getCode());
                            item.sendMessage(item.session,toJSONString(item.threeResultWebSocket));
                            item.threeResultWebSocket.setCode(null);
                            item.onClose();
                            break;
                        }
//                        for(int i=0;i<item.threeResultWebSocket.getUserList().size();i++){
//                            if(item.threeResultWebSocket.getUserList().get(i).getThreeUser().getOpenId().equals(tiOpenId)){
//                                item.threeResultWebSocket.getUserList().remove(i);
//                            }
//                        }
//                        item.sendMessage(item.session,toJSONString(item.threeResultWebSocket));
                    }
                }
                return;
            }

        }catch (Exception e){
            log.error("onMessage error:{}", e);
        }
    }
    @OnError
    public void onError(Session session, Throwable throwable){
        log.error("发生错误:message:{}\n stack:{}", throwable.getMessage(), throwable.getStackTrace());
    }

    //   下面是自定义的一些方法
    public void sendMessage(Session session, String message) throws IOException {
        if (session.isOpen()){
            session.getBasicRemote().sendText(message);
        }

    }

    public static synchronized int getOnlineCount(){
        onlineCount = ThreeWebSock.webSocketSet.size();
        return onlineCount;
    }
    public static synchronized void addOnlineCount(){
        ThreeWebSock.onlineCount++;
    }
    public static synchronized void subOnlineCount(){
        ThreeWebSock.onlineCount--;
    }

    /**
     * 获取所有的队友(不包括自己)
     * @param webSock
     * @return
     */
    public static List<ThreeWebSock> getAllWebSockDelMe(ThreeWebSock webSock){
        //
        List<ThreeWebSock> webSocks = new ArrayList<ThreeWebSock>();
        webSocketSet.forEach(item -> {
            if (item != null && item.threeResultWebSocket != null){
                //不是自己
                if(!item.threeResultWebSocket.getUserResult().getThreeUser().getOpenId().equals(webSock.threeResultWebSocket.getUserResult().getThreeUser().getOpenId())){
                    if (item.threeResultWebSocket.getRoom() == webSock.threeResultWebSocket.getRoom()){
                        webSocks.add(item);
                    }
                }
            }
        });
        return webSocks;
    }

    /**
     * 获取所有的队友(包括自己)
     * @param webSock
     * @return
     */
    public static List<ThreeWebSock> getAllWebSock(ThreeWebSock webSock){
        //
        List<ThreeWebSock> webSocks = new ArrayList<ThreeWebSock>();
        webSocketSet.forEach(item -> {
            if (item != null && item.threeResultWebSocket != null){
                //不是自己
                if (item.threeResultWebSocket.getRoom() == webSock.threeResultWebSocket.getRoom()){
                    webSocks.add(item);
                }
            }
        });
        return webSocks;
    }


    public static String toJSONString(Object object){
        return JSONObject.toJSONString(object,SerializerFeature.DisableCircularReferenceDetect);
    }

}
