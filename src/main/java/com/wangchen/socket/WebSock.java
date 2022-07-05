package com.wangchen.socket;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.User;
import com.wangchen.common.*;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.ComputeUtils;
import com.wangchen.vo.OneVsOneTopicVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@ServerEndpoint("/websocket/{openId}/{roomId}")
@Component
public class WebSock {
    private static int onlineCount = 0;
    private static CopyOnWriteArrayList<WebSock> webSocketSet = new CopyOnWriteArrayList<>();
    private Session session;
    private boolean isGame;

    // 题目列表
    private List<OneVsOneTopicVo> answerResponse;
    private ResultWebSocket resultWebSocket;
    // 是否重置用户答题信息 false：否 true:是
    private boolean isReset = false;
    // 是否接受到了超时
    private boolean isTimeOut = false;
    // 每次匹配限制次数
    private Integer index = 1000;

    private static RoomService roomService;

    @Autowired
    public void setRoomService(RoomService roomService) {
        WebSock.roomService = roomService;
    }

    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        WebSock.userService = userService;
    }

    private static Environment env;

    @Autowired
    public void setEnv(Environment env) {
        WebSock.env = env;
    }

    private static UserHonorService userHonorService;

    @Autowired
    public void setUserHonorService(UserHonorService userHonorService) {
        WebSock.userHonorService = userHonorService;
    }

    private static UserLevelService userLevelService;

    @Autowired
    public void setUserLevelService(UserLevelService userLevelService) {
        WebSock.userLevelService = userLevelService;
    }

    private static UserOneVsOneLogService userOneVsOneLogService;

    @Autowired
    public void setUserOneVsOneLogService(UserOneVsOneLogService userOneVsOneLogService) {
        WebSock.userOneVsOneLogService = userOneVsOneLogService;
    }

    private static BranchTopicService branchTopicService;

    @Autowired
    public void setBranchTopicService(BranchTopicService branchTopicService) {
        WebSock.branchTopicService = branchTopicService;
    }

    private static BranchOptionService branchOptionService;

    @Autowired
    public void setBranchOptionService(BranchOptionService branchOptionService) {
        WebSock.branchOptionService = branchOptionService;
    }

    private static ComputeUtils computeUtils;

    @Autowired
    public void setComputeUtils(ComputeUtils computeUtils) {
        WebSock.computeUtils = computeUtils;
    }

    private static UserAchievementService userAchievementService;

    @Autowired
    public void setUserAchievementService(UserAchievementService userAchievementService) {
        WebSock.userAchievementService = userAchievementService;
    }

    private static UserBranchService userBranchService;

    @Autowired
    public void setUserBranchService(UserBranchService userBranchService) {
        WebSock.userBranchService = userBranchService;
    }

    private static FeiBranchTopicService feiBranchTopicService;

    @Autowired
    public void setFeiBranchTopicService(FeiBranchTopicService feiBranchTopicService) {
        WebSock.feiBranchTopicService = feiBranchTopicService;
    }

    private static FeiBranchOptionService feiBranchOptionService;

    @Autowired
    public void setFeiBranchOptionService(FeiBranchOptionService feiBranchOptionService) {
        WebSock.feiBranchOptionService = feiBranchOptionService;
    }


    /**
     *  用户进入房间、或受邀进入房间
     *  将房主，受邀人员通信对象防止于通信集合中，以房间号区分配对情况
     *
     * @param openId 用户id
     * @param roomId 房间号
     * @param session 会话
     */
    @OnOpen
    public void onOpen(@PathParam("openId") String openId, @PathParam("roomId") long roomId, Session session) {
        try {
            log.info("connect 1v1 onOpen:openId:{}", openId);
            // 房主
            if (0 == roomId) {
                // 用户不存在
                com.wangchen.entity.User member = userService.getUserByOpenId(openId);
                if (null == member) {
                    sendMessage(session, JSONObject.toJSONString(Result.newFail(BusinessErrorMsg.USER_TOKEN_IS_NULL)));
                    return;
                }
                // 当前人是作为房主准备邀请人或进行匹配的，所以已经在集合里了的话需先删除(关闭会话)
                for (WebSock webSock : webSocketSet) {
                    if (webSock.resultWebSocket.getUserResult().getUser().getOpenId().equals(member.getOpenId())) {
                        if (!webSock.isGame) {
                            webSock.onClose();
                            break;
                        }
                    }
                }
                // 准备就绪
                this.session = session;

                User user = new User();
                user.setOpenId(openId);
                user.setNickName(member.getName());
                user.setAvatar(member.getAvatar());
                user.setOpenId(member.getOpenId());
                UserHonor userHonor = userHonorService.getLastHonor(user.getOpenId());
                user.setHonorId(null == userHonor ? 0 : userHonor.getHonorId());
                user.setHonorName(null == userHonor ? "未获段位" : userHonor.getHonorName());
                UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id", member.getOpenId()));
                user.setLevel(null == userLevel ? 0 : userLevel.getLevelId() - 1);
                //成就
                List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id", openId).orderByDesc("create_time"));
                if (CollectionUtils.isEmpty(userAchievementList)) {
                    user.setAchievementName("未获成就");
                } else {
                    user.setAchievementName(userAchievementList.get(0).getAchievementName());
                }

                UserResult userResult = new UserResult();
                userResult.setUser(user);
                userResult.setScore(0);
                userResult.setIs_answer(-1);
                Long maxRoomId = roomService.selectMaxRoomNo();
                Room room = new Room();
                room.setRoomNo(null == maxRoomId ? 1 : maxRoomId + 1);
                room.setCreateDate(new Date());
                room.setIsOpen(0);
                roomService.save(room);

                ResultWebSocket resultWebSocket = new ResultWebSocket();
                resultWebSocket.setRoom(room.getRoomNo());
                resultWebSocket.setIsHouseOwner(1);
                resultWebSocket.setUserResult(userResult);
                resultWebSocket.setGameState(0);
                resultWebSocket.setBlueUserResult(userResult);
                resultWebSocket.setRedUserResult(null);
                resultWebSocket.setPiPei(false);
                this.resultWebSocket = resultWebSocket.clone();
                // 发送信息
                this.sendMessage(this.session, JSONObject.toJSONString(this.resultWebSocket, SerializerFeature.DisableCircularReferenceDetect));
                // 加入set中
                if (!webSocketSet.contains(this)) {
                    webSocketSet.add(this);
                }
            } else {
                // 受邀请方
                // 用户不存在
                com.wangchen.entity.User member = userService.getUserByOpenId(openId);
                if (null == member) {
                    sendMessage(session, JSONObject.toJSONString(Result.newFail(BusinessErrorMsg.USER_TOKEN_IS_NULL)));
                    return;
                }
                // 查询该人员是否已在整体某个房间中
                for (WebSock webSock : webSocketSet) {
                    if (webSock.resultWebSocket.getUserResult().getUser().getOpenId().equals(openId)) {
                        webSock.onClose();
                        break;
                    }
                }
                // 获取到房间里所有的人
                List<WebSock> readys = new ArrayList<WebSock>();
                // 检查当前房间号是否有效(20220614匹配错误BUG修正)
                Room oldRoom = roomService.getOne(new QueryWrapper<Room>().eq("room_no", roomId).isNull("end_time"));
                if (null != oldRoom) {
                    for (WebSock webSock : webSocketSet) {
                        // 获取房间中的房主
                        if (roomId == webSock.resultWebSocket.getRoom()) {
                            readys.add(webSock);
                        }
                    }
                }
                //(20220614匹配错误BUG修正)
                // 房主已经退出，房间已经不存在
                if (readys.size() <= 0) {
                    sendMessage(session, JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.ROOM_IS_NULL.getCode(), BussinessErrorMsg.ROOM_IS_NULL.getMsg())));
                    return;
                }
                // 如果房间里人数>1,说明房主邀请其他人参与1v1，且得到回应已进入房间准备开始比赛
                if (readys.size() > 1) {
                    log.info("房间已经满员");
                    sendMessage(session, JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.ROOM_YI_JING_MAN_YUAN.getCode(), BussinessErrorMsg.ROOM_YI_JING_MAN_YUAN.getMsg())));
                    return;
                }
                // 房主已经重新进行匹配，该房间已经失效
                if (readys.get(0).resultWebSocket.isPiPei()) {
                    sendMessage(session, JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.PVP_ROOM_IS_PEIPEI_ZHONG_ROOM.getCode(), BussinessErrorMsg.PVP_ROOM_IS_PEIPEI_ZHONG_ROOM.getMsg())));
                    return;
                }

                // 准备就绪
                this.session = session;
                User user = new User();
                user.setOpenId(openId);
                user.setNickName(member.getName());
                user.setAvatar(member.getAvatar());
                UserHonor userHonor = userHonorService.getLastHonor(user.getOpenId());
                user.setHonorId(null == userHonor ? 0 : userHonor.getHonorId());
                user.setHonorName(null == userHonor ? "未获段位" : userHonor.getHonorName());
                UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id", member.getOpenId()));
                user.setLevel(null == userLevel ? 0 : userLevel.getLevelId() - 1);
                //成就
                List<UserAchievement> userAchievementList = userAchievementService.list(new QueryWrapper<UserAchievement>().eq("open_id", openId).orderByDesc("create_time"));
                if (CollectionUtils.isEmpty(userAchievementList)) {
                    user.setAchievementName("未获成就");
                } else {
                    user.setAchievementName(userAchievementList.get(0).getAchievementName());
                }

                UserResult userResult = new UserResult();
                userResult.setUser(user);
                userResult.setScore(0);
                userResult.setIs_answer(-1);

                ResultWebSocket resultWebSocket = new ResultWebSocket();
                resultWebSocket.setRoom(roomId);
                resultWebSocket.setIsHouseOwner(0);
                resultWebSocket.setUserResult(userResult);
                resultWebSocket.setGameState(0);
                resultWebSocket.setRedUserResult(userResult);
                resultWebSocket.setPiPei(false);
                this.resultWebSocket = resultWebSocket.clone();
                readys.add(this);

                // 完善对战双方UserResult对象信息
                // 蓝色方、房主
                readys.get(0).resultWebSocket.setRedUserResult(this.resultWebSocket.getUserResult());
                readys.get(0).sendMessage(readys.get(0).session, toJSONString(readys.get(0).resultWebSocket));
                // 红色方，受邀请方
                readys.get(1).resultWebSocket.setBlueUserResult(readys.get(0).resultWebSocket.getBlueUserResult());
                readys.get(1).sendMessage(readys.get(1).session, toJSONString(readys.get(1).resultWebSocket));

                webSocketSet.add(this);//加入set中
            }

            addOnlineCount();
            log.info("有新连接加入！当前在线人数为:{},openId:{}\n session:{}", getOnlineCount(), openId, session);
        } catch (Exception e) {
            log.error("onOpen error:{}", e);
        }
    }


    /**
     * 匹配用户逻辑
     *
     * @Return: boolean 是否匹配成功
     */
    private boolean matchUser() throws IOException {
        synchronized (WebSock.class) {
            // 在游戏
            if (this.isGame) {
                return true;
            }
            // 未在匹配状态
            if (!this.resultWebSocket.isPiPei()) {
                return true;
            }
            // 进行匹配
            return match();
        }
    }


    /**
     * 对战匹配
     * 匹配到就立即开始对战，此过程已经完成选题过程，初始化相关数据
     *
     * @Return: boolean 匹配是否成功
     */
    private boolean match() throws IOException {
        // 真人匹配
        if (webSocketSet.size() <= 1) {
            return false;
        }
        List<WebSock> readys = new ArrayList<>();
        for (WebSock webSock : webSocketSet) {
            // 非本人、在游戏中、匹配类型一致的用户
            log.debug("debug 20220610 webSock:{} this:{}", webSock, this);
            // 利用openId字段判断
            // 遍历到webSock openId
            String currOpenId = webSock.resultWebSocket.getUserResult().getUser().getOpenId();
            // 自身openId
            String nowOpenId = this.resultWebSocket.getUserResult().getUser().getOpenId();
            log.debug("debug 20220610 currOpenId :{}", currOpenId);
            log.debug("debug 20220610 nowOpenId :{}", nowOpenId);
            log.debug("debug 20220610 openId字段判断 :{}", currOpenId.equals(nowOpenId));
            // == 判断
            log.debug("debug 20220610 ==判断 :{}", webSock == this);
            // 由直接判断两个通信对象是否相等 改变为openId字段判断两个通信对象是否相等
            if (currOpenId.equals(nowOpenId) || webSock.isGame || !webSock.resultWebSocket.isPiPei()) {
                log.debug("debug 20220610 随机匹配避开自身、已经在游戏、不是匹配状态的人员");
                continue;
            }

            // 检查两人今天是否已经进行过1v1比赛
            if (userOneVsOneLogService.getFriendGameLog(Constants.SDF_YYYY_MM_DD.format(new Date()), nowOpenId, currOpenId)) {
                continue;
            }

            readys.add(webSock);
            log.debug("1v1竞赛匹配成功：{} - {}", nowOpenId, currOpenId);
            break;
        }
        // 没有匹配成功用户
        if (readys.size() < 1) {
            return false;
        }

        // 匹配用户集合
        List<WebSock> matchs = new ArrayList<>();
        matchs.add(readys.get(0));
        // 匹配成功
        matchs.add(this);

        // 匹配成功，修改用户状态
        matchs.forEach(item -> {
            item.isGame = true;
            // 用户将进入比赛，状态为未匹配状态
            item.resultWebSocket.setPiPei(false);
        });

        // 把被匹配到的用户房间给删除掉
        Long delRoom = matchs.get(0).resultWebSocket.getRoom();
        roomService.remove(new QueryWrapper<Room>().eq("room_no", delRoom.intValue()));

        // 获取题目信息 二期：获取各自ABC类题目
        UserResult blueUserResult = matchs.get(1).resultWebSocket.getUserResult();
        UserResult redUserResult = matchs.get(0).resultWebSocket.getUserResult();
        int blueCompanyType = blueUserResult.getUser().getType();
        int redCompanyType = redUserResult.getUser().getType();
        // *****原先的选题思维为AB对战人员各自选择所属公司类型下专业题库题目5题(已注释（选取题目部分）代码删除)

        // *****现修改为AB对战人员各自所属部门一获取题目3题，必知必会题目2题
        // 用于保存专业题
        List<BranchTopic> branchTopicList = new ArrayList<>();
        // 用于保存必知必会题
        List<FeiBranchTopic> feiBranchTopicList = new ArrayList<>();
        // 获取AB对战人员所属openid
        String blueOpenId = blueUserResult.getUser().getOpenId();
        String redOpenId = redUserResult.getUser().getOpenId();
        log.info("pipei_blueOpenId: {}", blueOpenId);
        log.info("pipei_redOpenId: {}", redOpenId);
        // 获取对战双方所属部门(只需要获取部门一)
        int blueBranchId = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id", blueOpenId).orderByAsc("id")).get(0).getBranchId();
        int redBranchId = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id", redOpenId).orderByAsc("id")).get(0).getBranchId();
        // 获得用户部门对应答题部门id
        int blueBranchId01 = Constants.newBranchTypeMap.get(blueBranchId)[1];
        int redBranchId01 = Constants.newBranchTypeMap.get(redBranchId)[1];
        if (blueCompanyType == redCompanyType) {
            // 所属公司类型相同
            if (blueBranchId01 == redBranchId01) {
                // 对战双方答题专业题库相同（6道题）
                if (0 == blueBranchId01) {
                    // 如果对战双方是公司领导，则在专业题库选6题
                    List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(6, 1);
                    branchTopicList.addAll(currBranchTopicList);
                } else {
                    // 对战双方不是公司领导，在答题题库选6题
                    List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 6);
                    branchTopicList.addAll(currBranchTopicList);
                }
            } else {
                if (0 == blueBranchId01) {
                    // 如果蓝色对战方是公司领导，则在专业题库选3题
                    List<BranchTopic> blueBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, 1);
                    branchTopicList.addAll(blueBranchTopicList);
                } else {
                    // 如果蓝色对战方不是是公司领导，在答题题库选3题
                    List<BranchTopic> blueBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 3);
                    branchTopicList.addAll(blueBranchTopicList);
                }
                if (0 == redBranchId01) {
                    // 如果红色对战方是公司领导，则在专业题库选3题
                    List<BranchTopic> redBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, 1);
                    branchTopicList.addAll(redBranchTopicList);
                } else {
                    // 如果红色对战方不是是公司领导，在答题题库选3题
                    List<BranchTopic> redBranchTopicList = branchTopicService.listTopicRandom(redBranchId01, 3);
                    branchTopicList.addAll(redBranchTopicList);
                }
            }
            // 必知必会4道题
            List<FeiBranchTopic> currFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(4, 1);
            feiBranchTopicList.addAll(currFeiBranchTopicList);
        } else {
            // 所属公司类型不同（所属公司下每人3道专业题，2道必知必会题）
            if (0 == blueBranchId01) {
                // 如果蓝色对战方是公司领导，则在专业题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, blueCompanyType);
                branchTopicList.addAll(currBranchTopicList);
            } else {
                // 蓝色对战方不是公司领导，在答题题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 3);
                branchTopicList.addAll(currBranchTopicList);
            }
            if (0 == redBranchId01) {
                // 如果蓝色对战方是公司领导，则在专业题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, redCompanyType);
                branchTopicList.addAll(currBranchTopicList);
            } else {
                // 蓝色对战方不是公司领导，在答题题库选3题
                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(redBranchId01, 3);
                branchTopicList.addAll(currBranchTopicList);
            }

            // 必知必会4道题
            List<FeiBranchTopic> blueFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(2, blueCompanyType);
            List<FeiBranchTopic> redFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(2, redCompanyType);
            feiBranchTopicList.addAll(blueFeiBranchTopicList);
            feiBranchTopicList.addAll(redFeiBranchTopicList);
        }
        // 判断获取题目是否满足数量需求
        if (6 != branchTopicList.size() || 4 != feiBranchTopicList.size()) {
            log.error("题目数量不够");
            return false;
        }

        List<OneVsOneTopicVo> topicList = new ArrayList<OneVsOneTopicVo>();
        int rankNo = 1;
        // 处理获取题目信息(专业题)
        for (BranchTopic branchTopic : branchTopicList) {
            OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
            BeanUtils.copyProperties(branchTopic, oneVsOneTopicVo);
            oneVsOneTopicVo.setTypeName(Constants.newBuMenKu.get(branchTopic.getType())[1]);
            oneVsOneTopicVo.setRankNo(rankNo++);
            List<BranchOption> currBranchOptionList = branchOptionService.list(new QueryWrapper<BranchOption>().eq("topic_id", branchTopic.getId()));
            List<BranchOption> branchOptionList = new ArrayList<BranchOption>();
            for (BranchOption branchOption : currBranchOptionList) {
                if (2 == branchTopic.getTopicType()) {
                    BranchOption currBranchOption = new BranchOption();
                    BeanUtils.copyProperties(branchOption, currBranchOption);
                    String currStr = branchOption.getContent();
                    if ("对".equals(currStr)) {
                        currBranchOption.setContent("正确");
                    } else {
                        currBranchOption.setContent("错误");
                    }
                    branchOptionList.add(currBranchOption);
                } else {
                    branchOptionList.add(branchOption);
                }
            }
            oneVsOneTopicVo.setOptionList(branchOptionList);
            topicList.add(oneVsOneTopicVo);
        }

        // 处理获取题目信息(必知必会题)
        rankNo = topicList.size();
        for (FeiBranchTopic feiBranchTopic : feiBranchTopicList) {
            OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
            BeanUtils.copyProperties(feiBranchTopic, oneVsOneTopicVo);
            oneVsOneTopicVo.setParse(feiBranchTopic.getCorrectParse());
            oneVsOneTopicVo.setTypeName("应知应会");
            oneVsOneTopicVo.setCreateTime(new Date());
            oneVsOneTopicVo.setRankNo(++rankNo);
            List<FeiBranchOption> feiBranchOptionList = feiBranchOptionService.list(new QueryWrapper<FeiBranchOption>().eq("topic_id", feiBranchTopic.getId()));
            List<BranchOption> branchOptionList = new ArrayList<>();
            if (!CollUtil.isEmpty(feiBranchOptionList)) {
                for (FeiBranchOption feiBranchOption : feiBranchOptionList) {
                    BranchOption branchOption = new BranchOption();
                    BeanUtils.copyProperties(feiBranchOption, branchOption);
                    // 对每日答题判断题返回"正确"、"错误"信息处理
                    if (2 == feiBranchTopic.getTopicType()) {
                        if ("对".equals(feiBranchOption.getContent())) {
                            branchOption.setContent("正确");
                        } else {
                            branchOption.setContent("错误");
                        }
                    }
                    branchOptionList.add(branchOption);
                }
            }
            oneVsOneTopicVo.setOptionList(branchOptionList);
            topicList.add(oneVsOneTopicVo);
        }

        log.info("pipei_tittle, {}", topicList);
        // 被匹配到的人状态修改为房间玩家，房间号设置为当前房间号;
        matchs.get(0).resultWebSocket.setIsHouseOwner(0);
        matchs.get(0).resultWebSocket.setRoom(matchs.get(1).resultWebSocket.getRoom());
        // 初始化房主答题数据
        matchs.get(1).resultWebSocket.setBlueUserResult(matchs.get(1).resultWebSocket.getUserResult());
        matchs.get(1).resultWebSocket.setRedUserResult(matchs.get(0).resultWebSocket.getUserResult());
        // 初始化房间玩家答题数据
        matchs.get(0).resultWebSocket.setBlueUserResult(matchs.get(1).resultWebSocket.getUserResult());
        matchs.get(0).resultWebSocket.setRedUserResult(matchs.get(0).resultWebSocket.getUserResult());
        // 初始化房主、房间玩家通信信息
        for (WebSock item : matchs) {
            item.answerResponse = topicList;
            item.isGame = true;
            item.resultWebSocket.setGameState(1);
            item.resultWebSocket.setTopicId(topicList.get(0).getId());

            item.sendMessage(item.session, JSONObject.toJSONString(Result.newSuccess(BusinessErrorMsg.ANSWER, item.answerResponse)));
            item.sendMessage(item.session, toJSONString(item.resultWebSocket));
        }

        Room room = roomService.getOne(new QueryWrapper<Room>().eq("room_no", this.resultWebSocket.getRoom()));
        if (null != room) {
            room.setIsOpen(1);
            room.setOpenTime(new Date());
            roomService.updateById(room);
        }
        //记录下本房间的两个人
        UserOneVsOneLog userOneVsOneLog = new UserOneVsOneLog();
        userOneVsOneLog.setRoomId(room.getRoomNo().intValue());
        userOneVsOneLog.setRoomOpenId(this.resultWebSocket.getUserResult().getUser().getOpenId());
        userOneVsOneLog.setRoomOpenScore(0);
        userOneVsOneLog.setFriendOpenId(this.resultWebSocket.getRedUserResult().getUser().getOpenId());
        userOneVsOneLog.setFriendOpenScore(0);
        userOneVsOneLog.setCreateDate(new Date());
        userOneVsOneLog.setIsWin(0);
        userOneVsOneLog.setCreateTime(new Date());
        userOneVsOneLogService.save(userOneVsOneLog);
        return true;
    }


    /**
     * 退出个人赛对战
     */
    @OnClose
    public void onClose() {
        // 个人赛在线人数-1
        subOnlineCount();
        synchronized (this) {
            webSocketSet.remove(this);
            // 既没有进行匹配，也没有邀请对战，直接退出就可以了
            if (null == this.resultWebSocket) {
                return;
            }
            // 退出前为匹配状态，则退出后修改状态为未匹配
            if (this.resultWebSocket.isPiPei()) {
                this.resultWebSocket.setPiPei(false);
            }
            try {
                // 获取当前房间用户
                List<WebSock> webSocks = getAllWebSockDelMe(this);
                // 我在游戏状态退出
                if (this.isGame) {
                    for (WebSock webSock : webSocks) {
                        // 我是房主(红方)
                        if (webSock.resultWebSocket.getRedUserResult().getUser().getOpenId().equals(this.resultWebSocket.getUserResult().getUser().getOpenId())) {
                            // webSock.resultWebSocket.setRedUserResult(null);
                            webSock.resultWebSocket.getRedUserResult().setScore(0);
                            webSock.resultWebSocket.getRedUserResult().setIs_answer(2);
                            webSock.resultWebSocket.getRedUserResult().setSelectAnswer("timeOut");
                            webSock.resultWebSocket.getRedUserResult().setSelectAnswerOpt("timeOut");
                            webSock.resultWebSocket.getRedUserResult().setSelectAnswerOpt2("timeOut");

                            // 如果未答完题且当前得分为0，则将成绩设置为5分，用于区分答完题目却得0分情况
                            if (0 == webSock.resultWebSocket.getUserResult().getScore()) {
                                webSock.resultWebSocket.getUserResult().setScore(5);
                                webSock.resultWebSocket.getBlueUserResult().setScore(5);
                            }
                        }
                        // 我是房间玩家(蓝色方)
                        if (webSock.resultWebSocket.getBlueUserResult().getUser().getOpenId().equals(this.resultWebSocket.getUserResult().getUser().getOpenId())) {
                            // webSock.resultWebSocket.setBlueUserResult(null);
                            webSock.resultWebSocket.getBlueUserResult().setScore(0);
                            webSock.resultWebSocket.getBlueUserResult().setIs_answer(2);
                            webSock.resultWebSocket.getBlueUserResult().setSelectAnswer("timeOut");
                            webSock.resultWebSocket.getBlueUserResult().setSelectAnswerOpt("timeOut");
                            webSock.resultWebSocket.getBlueUserResult().setSelectAnswerOpt2("timeOut");

                            // 如果未答完题且当前得分为0，则将成绩设置为5分，用于区分答完题目却得0分情况
                            if (webSock.resultWebSocket.getUserResult().getScore() == 0) {
                                webSock.resultWebSocket.getUserResult().setScore(5);
                                webSock.resultWebSocket.getRedUserResult().setScore(5);
                            }
                        }

                        // 对方离开了，也要在对方界面显示"有玩家离开游戏"提示信息？？
                        webSock.resultWebSocket.setCode(BussinessErrorMsg.PLAYER_LEAVE.getCode());
                        webSock.sendMessage(webSock.session, toJSONString(webSock.resultWebSocket));
                        webSock.resultWebSocket.setCode(null);

                        // 有人离开，对战就结束了，更新房间信息
                        Room room = roomService.getOne(new QueryWrapper<Room>()
                                .eq("room_no", webSock.resultWebSocket.getRoom()));
                        room.setEndTime(new Date());
                        roomService.updateById(room);

                        UserOneVsOneLog userOneVsOneLog = userOneVsOneLogService.getOne(new QueryWrapper<UserOneVsOneLog>().eq("room_id", room.getRoomNo()));
                        if (userOneVsOneLog.getRoomOpenId().equals(webSock.resultWebSocket.getUserResult().getUser().getOpenId())) {
                            //如果剩下的房主
                            userOneVsOneLog.setRoomOpenScore(webSock.resultWebSocket.getUserResult().getScore());
                            userOneVsOneLog.setFriendOpenScore(0);
                            userOneVsOneLog.setIsWin(1);
                        } else {
                            //如果剩下的房间玩家
                            userOneVsOneLog.setRoomOpenScore(0);
                            userOneVsOneLog.setFriendOpenScore(webSock.resultWebSocket.getUserResult().getScore());
                            userOneVsOneLog.setIsWin(2);
                        }
                        userOneVsOneLogService.updateById(userOneVsOneLog);

                        // 更新積分塔幣，前五次：胜者2经验2塔币，败者1经验0塔币; 6-10次：胜者2经验0塔币，败者1经验0塔币; 大于10次没有经验塔币奖励
                        // 剩下的玩家 算赢
                        if (userOneVsOneLogService.getGameLogNumByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),
                                webSock.resultWebSocket.getUserResult().getUser().getOpenId()) <= 5) {
                            com.wangchen.entity.User user = userService.getUserByOpenId(webSock.resultWebSocket.getUserResult().getUser().getOpenId());
                            computeUtils.computeGame2(webSock.resultWebSocket.getUserResult().getUser().getOpenId(), 2, 2, user);
                            webSock.resultWebSocket.setGetExperienceNum(2);
                            webSock.resultWebSocket.setGetCoinNum(2);
                        } else if (userOneVsOneLogService.getGameLogNumByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),
                                webSock.resultWebSocket.getUserResult().getUser().getOpenId()) <= 10) {
                            webSock.resultWebSocket.setGetExperienceNum(2);
                            webSock.resultWebSocket.setGetCoinNum(0);
                        } else {
                            webSock.resultWebSocket.setGetExperienceNum(0);
                            webSock.resultWebSocket.setGetCoinNum(0);
                        }

                        webSock.resultWebSocket.setGameState(3);
                        webSock.sendMessage(webSock.session, toJSONString(webSock.resultWebSocket));
                    }
                    // 更新房间2人经验、塔币
                    if (userOneVsOneLogService.getGameLogNumByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),
                            this.resultWebSocket.getUserResult().getUser().getOpenId()) <= 10) {
                        com.wangchen.entity.User user = userService.getUserByOpenId(this.resultWebSocket.getUserResult().getUser().getOpenId());
                        computeUtils.computeGame2(this.resultWebSocket.getUserResult().getUser().getOpenId(), 1, 0, user);
                    }
                } else {
                    // 我不是在游戏状态退出
                    for (WebSock webSock : webSocks) {
                        // 我是房主
                        if (1 == this.resultWebSocket.getIsHouseOwner()) {
                            webSock.resultWebSocket.setIsHouseOwner(1);
                            webSock.resultWebSocket.setBlueUserResult(webSock.resultWebSocket.getRedUserResult());
                            webSock.resultWebSocket.setRedUserResult(null);
                        } else {
                            // 我是红色方
                            if (webSock.resultWebSocket.getRedUserResult().getUser().getOpenId().equals(this.resultWebSocket.getUserResult().getUser().getOpenId())) {
                                webSock.resultWebSocket.setRedUserResult(null);
                            }
                            // 我是蓝色方？前面已经判定不是房主，相互冲突，但不影响，不会执行
                            if (webSock.resultWebSocket.getBlueUserResult().getUser().getOpenId().equals(this.resultWebSocket.getUserResult().getUser().getOpenId())) {
                                webSock.resultWebSocket.setBlueUserResult(null);
                            }
                        }

                        webSock.sendMessage(webSock.session, toJSONString(webSock.resultWebSocket));
                    }

                    // 更新房间信息，设置结束时间 (20220614匹配错误BUG修正)
                    Room lastRoom = roomService.getOne(new QueryWrapper<Room>().eq("room_no", this.resultWebSocket.getRoom()));
                    if (null != lastRoom) {
                        lastRoom.setEndTime(new Date());
                        roomService.updateById(lastRoom);
                        log.debug("更新房间信息，设置结束时间 (20220614匹配错误BUG修正) RoomNo:{}", lastRoom.getRoomNo());
                    }
                    //(20220614匹配错误BUG修正)
                }
            } catch (Exception e) {
                log.error("用户退出出现错误： 错误信息:{}", e.getMessage());
            }

        }
        log.info("有一连接关闭！当前在线人数为,count:{}\n session:{}", getOnlineCount(), session);
    }


    /**
     * 消息处理
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        synchronized (WebSock.class) {
            try {
                log.info("{}来自客户端的消息:{}\n session:{}", this.resultWebSocket.getUserResult().getUser().getNickName(), message, session);
                if (!JSONObject.isValid(message)) {
                    return;
                }
                // 解析对象
                JSONObject json = JSONObject.parseObject(message);
                String type = json.getString("type");

                // 心跳 pong、聊天 chat、答题 answer
                if ("pong".equals((type))) {
                    this.sendMessage(session,
                            // JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.PONG_ONE.getCode(),BussinessErrorMsg.PONG_ONE.getMsg()))
                            //二期：在心跳包的返回中添加在线人数统计
                            JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.PONG_ONE.getCode(), "" + getOnlineCount()))
                    );
                    return;
                }

                // 匹配
                if ("pipei".equals(type)) {
                    this.resultWebSocket.setPiPei(true);
                    this.sendMessage(this.session, toJSONString(this.resultWebSocket));
                    // 定时器
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                // 匹配用户
                                if (matchUser()) {
                                    timer.cancel();
                                }
                                // 匹配次数减少
                                index--;
                            } catch (Exception e) {
                                index--;
                                log.error("timer error:{}", e);
                            }
                        }
                    }, 0, 1000);
                    return;
                }

                if ("suspendPipei".equals(type)) {
                    this.resultWebSocket.setPiPei(false);
                    this.sendMessage(this.session, toJSONString(this.resultWebSocket));
                    index = 600;
                    return;
                }

                // 点击开始
                if ("start".equals(type)) {
                    UserResult blueUserResult = this.resultWebSocket.getBlueUserResult();
                    UserResult redUserResult = this.resultWebSocket.getRedUserResult();
                    // 一个人是不能开始游戏的
                    if (null == blueUserResult || null == redUserResult) {
                        this.resultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_ONE.getCode());
                        this.sendMessage(this.session, toJSONString(this.resultWebSocket));
                        this.resultWebSocket.setCode(null);
                        return;
                    }
                    // 如果两人今天已经玩过了 test
                    if (userOneVsOneLogService.getFriendGameLog(
                            Constants.SDF_YYYY_MM_DD.format(new Date()), blueUserResult.getUser().getOpenId(), redUserResult.getUser().getOpenId())) {
                        this.resultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_ONE_VS_ONE.getCode());
                        this.sendMessage(this.session, toJSONString(this.resultWebSocket));
                        this.resultWebSocket.setCode(null);
                        return;
                    }

                    // 获取题目信息 二期：获取各自ABC类题目 （过时代码，删除）
                    // *****现修改为AB对战人员各自所属部门一获取题目3题，必知必会题目2题
                    int blueCompanyType = blueUserResult.getUser().getType();
                    int redCompanyType = redUserResult.getUser().getType();
                    // 用于保存专业题
                    List<BranchTopic> branchTopicList = new ArrayList<>();
                    // 用于保存必知必会题
                    List<FeiBranchTopic> feiBranchTopicList = new ArrayList<>();
                    // 获取AB对战人员所属openid
                    String blueOpenId = blueUserResult.getUser().getOpenId();
                    String redOpenId = redUserResult.getUser().getOpenId();
                    log.info("yqoqing_blueOpenId: {}", blueOpenId);
                    log.info("yqoqing_redOpenId: {}", redOpenId);
                    // 获取对战双方所属部门(只需要获取部门一)
                    int blueBranchId = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id", blueOpenId).orderByAsc("id")).get(0).getBranchId();
                    int redBranchId = userBranchService.list(new QueryWrapper<UserBranch>().eq("open_id", redOpenId).orderByAsc("id")).get(0).getBranchId();
                    // 获得用户部门对应答题部门id
                    int blueBranchId01 = Constants.newBranchTypeMap.get(blueBranchId)[1];
                    int redBranchId01 = Constants.newBranchTypeMap.get(redBranchId)[1];
                    if (blueCompanyType == redCompanyType) {
                        // 所属公司类型相同
                        if (blueBranchId01 == redBranchId01) {
                            // 对战双方答题专业题库相同（6道题）
                            if (0 == blueBranchId01) {
                                // 如果对战双方是公司领导，则在专业题库选6题
                                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(6, 1);
                                branchTopicList.addAll(currBranchTopicList);
                            } else {
                                // 对战双方不是公司领导，在答题题库选6题
                                List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 6);
                                branchTopicList.addAll(currBranchTopicList);
                            }
                        } else {
                            if (0 == blueBranchId01) {
                                // 如果蓝色对战方是公司领导，则在专业题库选3题
                                List<BranchTopic> blueBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, 1);
                                branchTopicList.addAll(blueBranchTopicList);
                            } else {
                                // 如果蓝色对战方不是是公司领导，在答题题库选3题
                                List<BranchTopic> blueBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 3);
                                branchTopicList.addAll(blueBranchTopicList);
                            }
                            if (0 == redBranchId01) {
                                // 如果红色对战方是公司领导，则在专业题库选3题
                                List<BranchTopic> redBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, 1);
                                branchTopicList.addAll(redBranchTopicList);
                            } else {
                                // 如果红色对战方不是是公司领导，在答题题库选3题
                                List<BranchTopic> redBranchTopicList = branchTopicService.listTopicRandom(redBranchId01, 3);
                                branchTopicList.addAll(redBranchTopicList);
                            }
                        }
                        // 必知必会4道题
                        List<FeiBranchTopic> currFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(4, 1);
                        feiBranchTopicList.addAll(currFeiBranchTopicList);
                    } else {
                        // 所属公司类型不同（所属公司下每人3道专业题，2道必知必会题）
                        if (0 == blueBranchId01) {
                            // 如果蓝色对战方是公司领导，则在专业题库选3题
                            List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, blueCompanyType);
                            branchTopicList.addAll(currBranchTopicList);
                        } else {
                            // 蓝色对战方不是公司领导，在答题题库选3题
                            List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(blueBranchId01, 3);
                            branchTopicList.addAll(currBranchTopicList);
                        }
                        if (0 == redBranchId01) {
                            // 如果蓝色对战方是公司领导，则在专业题库选3题
                            List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandomByCompanyType(3, redCompanyType);
                            branchTopicList.addAll(currBranchTopicList);
                        } else {
                            // 蓝色对战方不是公司领导，在答题题库选3题
                            List<BranchTopic> currBranchTopicList = branchTopicService.listTopicRandom(redBranchId01, 3);
                            branchTopicList.addAll(currBranchTopicList);
                        }

                        // 必知必会4道题
                        List<FeiBranchTopic> blueFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(2, blueCompanyType);
                        List<FeiBranchTopic> redFeiBranchTopicList = feiBranchTopicService.listTopicRandomByCompanyType(2, redCompanyType);
                        feiBranchTopicList.addAll(blueFeiBranchTopicList);
                        feiBranchTopicList.addAll(redFeiBranchTopicList);
                    }
                    // 判断获取题目是否满足需求
                    if (6 != branchTopicList.size() || 4 != feiBranchTopicList.size()) {
                        log.error("题目数量不够");
                        return;
                    }

                    List<OneVsOneTopicVo> topicList = new ArrayList<OneVsOneTopicVo>();
                    int rankNo = 1;
                    // 处理获取题目信息(专业题)
                    for (BranchTopic branchTopic : branchTopicList) {
                        OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
                        BeanUtils.copyProperties(branchTopic, oneVsOneTopicVo);
                        oneVsOneTopicVo.setTypeName(Constants.newBuMenKu.get(branchTopic.getType())[1]);
                        oneVsOneTopicVo.setRankNo(rankNo++);
                        List<BranchOption> currBranchOptionList = branchOptionService.list(new QueryWrapper<BranchOption>().eq("topic_id", branchTopic.getId()));
                        List<BranchOption> branchOptionList = new ArrayList<BranchOption>();
                        for (BranchOption branchOption : currBranchOptionList) {
                            if (2 == branchTopic.getTopicType()) {
                                BranchOption currBranchOption = new BranchOption();
                                BeanUtils.copyProperties(branchOption, currBranchOption);
                                String currStr = branchOption.getContent();
                                if ("对".equals(currStr)) {
                                    currBranchOption.setContent("正确");
                                } else {
                                    currBranchOption.setContent("错误");
                                }
                                branchOptionList.add(currBranchOption);
                            } else {
                                branchOptionList.add(branchOption);
                            }
                        }
                        oneVsOneTopicVo.setOptionList(branchOptionList);
                        topicList.add(oneVsOneTopicVo);
                    }

                    // 处理获取题目信息(必知必会题)
                    rankNo = topicList.size();
                    for (FeiBranchTopic feiBranchTopic : feiBranchTopicList) {
                        OneVsOneTopicVo oneVsOneTopicVo = new OneVsOneTopicVo();
                        BeanUtils.copyProperties(feiBranchTopic, oneVsOneTopicVo);
                        oneVsOneTopicVo.setParse(feiBranchTopic.getCorrectParse());
                        oneVsOneTopicVo.setTypeName("应知应会");
                        oneVsOneTopicVo.setCreateTime(new Date());
                        oneVsOneTopicVo.setRankNo(++rankNo);
                        List<FeiBranchOption> feiBranchOptionList = feiBranchOptionService.list(new QueryWrapper<FeiBranchOption>().eq("topic_id", feiBranchTopic.getId()));
                        List<BranchOption> branchOptionList = new ArrayList<>();
                        if (!CollUtil.isEmpty(feiBranchOptionList)) {
                            for (FeiBranchOption feiBranchOption : feiBranchOptionList) {
                                BranchOption branchOption = new BranchOption();
                                BeanUtils.copyProperties(feiBranchOption, branchOption);
                                // 对每日答题判断题返回"正确"、"错误"信息处理
                                if (2 == feiBranchTopic.getTopicType()) {
                                    if ("对".equals(feiBranchOption.getContent())) {
                                        branchOption.setContent("正确");
                                    } else {
                                        branchOption.setContent("错误");
                                    }
                                }
                                branchOptionList.add(branchOption);
                            }
                        }
                        oneVsOneTopicVo.setOptionList(branchOptionList);
                        topicList.add(oneVsOneTopicVo);
                    }
                    log.info("yaoqing_tittle, {}", topicList);

                    List<WebSock> webSocks = getAllWebSock(this);
                    for (WebSock item : webSocks) {
                        item.answerResponse = topicList;
                        item.isGame = true;
                        item.resultWebSocket.setGameState(1);
                        item.resultWebSocket.setTopicId(topicList.get(0).getId());

                        item.sendMessage(item.session, JSONObject.toJSONString(Result.newSuccess(BusinessErrorMsg.ANSWER, item.answerResponse)));
                        item.sendMessage(item.session, toJSONString(item.resultWebSocket));
                    }

                    Room room = roomService.getOne(new QueryWrapper<Room>().eq("room_no", this.resultWebSocket.getRoom()));
                    if (null != room) {
                        room.setIsOpen(1);
                        room.setOpenTime(new Date());
                        roomService.updateById(room);
                    }
                    //记录下本房间的两个人
                    UserOneVsOneLog userOneVsOneLog = new UserOneVsOneLog();
                    userOneVsOneLog.setRoomId(room.getRoomNo().intValue());
                    userOneVsOneLog.setRoomOpenId(this.resultWebSocket.getUserResult().getUser().getOpenId());
                    userOneVsOneLog.setRoomOpenScore(0);
                    userOneVsOneLog.setFriendOpenId(this.resultWebSocket.getRedUserResult().getUser().getOpenId());
                    userOneVsOneLog.setFriendOpenScore(0);
                    userOneVsOneLog.setCreateDate(new Date());
                    userOneVsOneLog.setIsWin(0);
                    userOneVsOneLog.setCreateTime(new Date());
                    userOneVsOneLogService.save(userOneVsOneLog);
                    return;
                }

                // 答题
                if ("answer".equals(type)) {
                    String answer = json.getString("answer");//玩家回答的答案
                    if (StringUtils.isEmpty(answer)) {
                        return;
                    }
                    // 玩家回答的答案(填空题用)
                    String answer2 = json.getString("answer2");
                    // 题目id
                    Integer topicId = json.getInteger("topicId");
                    // 获取的选择的答案id
                    String answerId = json.getString("answerId");

                    // 如果已经答过题了就返回请勿重复答题
                    if (this.resultWebSocket.getUserResult().getIs_answer() == 1) {
                        this.sendMessage(session, JSONObject.toJSONString(Result.newFail(BussinessErrorMsg.PVP_ROOM_IS_HAS_ANSWER.getCode(), BussinessErrorMsg.PVP_ROOM_IS_HAS_ANSWER.getMsg())));
                        return;
                    }

                    //题目信息
                    OneVsOneTopicVo pacmanDrawTopicVo = null;
                    //循环所有题目
                    for (OneVsOneTopicVo topic : this.answerResponse) {
                        //根据传过来的id获取到题目信息(完善,除了id相同，题目所属专业、应知应会题库也要匹配)
                        if (topic.getId().intValue() == topicId.intValue()) {
                            pacmanDrawTopicVo = topic;
                            break;
                        }
                    }

                    boolean isAnswerTrue = false;

                    //选择题判断答题对错
                    if (pacmanDrawTopicVo == null) {
                        log.error("答案不属于任何题目 topicId:{}", topicId);
                        return;
                    } else if (0 == pacmanDrawTopicVo.getTopicType()) {
                        //遍历答案集合  用答案正确id和答案集合做对比
                        if (pacmanDrawTopicVo.getCorrectOptionId() == Integer.parseInt(answerId)) {
                            isAnswerTrue = true;
                        }
                    } else if (1 == pacmanDrawTopicVo.getTopicType()) {//填空题

                        if (pacmanDrawTopicVo.getOptionList().size() == 1) {
                            if (answer.equals(pacmanDrawTopicVo.getOptionList().get(0).getContent())) {
                                isAnswerTrue = true;
                            }
                        } else {
                            if (answer.equals(pacmanDrawTopicVo.getOptionList().get(0).getContent())
                                    && answer2.equals(pacmanDrawTopicVo.getOptionList().get(1).getContent())) {
                                isAnswerTrue = true;
                            }
                        }
                    } else if (2 == pacmanDrawTopicVo.getTopicType()) {//判断题
                        if (answer.equals(pacmanDrawTopicVo.getOptionList().get(0).getContent())) {
                            isAnswerTrue = true;
                        }
                    }

                    int gameStatus = 1;
                    //如果是最后一题 那就结束
                    if (topicId.intValue() == this.answerResponse.get(this.answerResponse.size() - 1).getId()) {
                        if (!this.resultWebSocket.getUserResult().getUser().getOpenId()
                                .equals(this.resultWebSocket.getBlueUserResult().getUser().getOpenId())) {
                            if (this.resultWebSocket.getBlueUserResult().getIs_answer() == 1 || this.resultWebSocket.getBlueUserResult().getIs_answer() == 2) {
                                gameStatus = 3;// 3是题目全部答完
                            }
                        } else {
                            if (this.resultWebSocket.getRedUserResult().getIs_answer() == 1 || this.resultWebSocket.getRedUserResult().getIs_answer() == 2) {
                                gameStatus = 3;// 3是题目全部答完
                            }
                        }
                    } else {
                        if (!this.resultWebSocket.getUserResult().getUser().getOpenId()
                                .equals(this.resultWebSocket.getBlueUserResult().getUser().getOpenId())) {
                            if (this.resultWebSocket.getBlueUserResult().getIs_answer() == 1 || this.resultWebSocket.getBlueUserResult().getIs_answer() == 2) {
                                gameStatus = 2;// 2是本道题答完 进行下一题
                            }
                        } else {
                            if (this.resultWebSocket.getRedUserResult().getIs_answer() == 1 || this.resultWebSocket.getRedUserResult().getIs_answer() == 2) {
                                gameStatus = 2;// 2是本道题答完 进行下一题
                            }
                        }
                    }


                    this.resultWebSocket.setCode(null);
                    this.isReset = false;
                    this.isTimeOut = false;
                    //答案正确
                    if (isAnswerTrue) {

                        Integer newSocre = this.resultWebSocket.getUserResult().getScore() + 10;

                        List<WebSock> webSockList = WebSock.getAllWebSock(this);
                        //把当前答题成功的人的情况告诉 别的玩家
                        for (WebSock item : webSockList) {
                            //同步 另外玩家里自己的状态
                            if (this.resultWebSocket.getUserResult().getUser().getOpenId().equals(item.resultWebSocket.getUserResult().getUser().getOpenId())) {
                                item.resultWebSocket.getUserResult().setIs_answer(1);//答对了
                                item.resultWebSocket.getUserResult().setScore(newSocre);//个人总分数
                                item.resultWebSocket.getUserResult().setSelectAnswer(
                                        StringUtils.isBlank(answerId) ? "" : answerId);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt(answer);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt2(answer2);
                            }

                            if (this.resultWebSocket.getUserResult().getUser().getOpenId().equals(item.resultWebSocket.getBlueUserResult().getUser().getOpenId())) {
                                item.resultWebSocket.getBlueUserResult().setScore(newSocre);//个人总分数
                                item.resultWebSocket.getUserResult().setSelectAnswer(StringUtils.isBlank(answerId) ? "" : answerId);
                                item.resultWebSocket.getBlueUserResult().setSelectAnswerOpt(answer);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt2(answer2);
                                item.resultWebSocket.getBlueUserResult().setIs_answer(1);
                            }

                            if (this.resultWebSocket.getUserResult().getUser().getOpenId().equals(item.resultWebSocket.getRedUserResult().getUser().getOpenId())) {
                                item.resultWebSocket.getRedUserResult().setScore(newSocre);//个人总分数
                                item.resultWebSocket.getUserResult().setSelectAnswer(StringUtils.isBlank(answerId) ? "" : answerId);
                                item.resultWebSocket.getRedUserResult().setSelectAnswerOpt(answer);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt2(answer2);
                                item.resultWebSocket.getRedUserResult().setIs_answer(1);
                            }
                            item.resultWebSocket.setGameState(gameStatus);
                            if (gameStatus == 3) {
                                //如果结束 需要设置一下未开始游戏 因为前端弹结算页面了，所以不需要在结束了，因为退出的时候会判断一下
                                //如果退出的时候gameStatus还是3的话 就结算，但实际是正常结束了(详情可以看 onclose方法)
                                item.isGame = false;
                            } else {
                                item.sendMessage(item.session, toJSONString(item.resultWebSocket));
                            }
                        }
                    } else {
                        List<WebSock> webSockList = WebSock.getAllWebSock(this);
                        //把当前答题成功的人的情况告诉 别的玩家
                        for (WebSock item : webSockList) {
                            //同步 另外玩家里自己的状态
                            if (this.resultWebSocket.getUserResult().getUser().getOpenId().equals(item.resultWebSocket.getUserResult().getUser().getOpenId())) {
                                item.resultWebSocket.getUserResult().setSelectAnswer(StringUtils.isBlank(answerId) ? "" : answerId);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt(answer);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt2(answer2);
                                item.resultWebSocket.getUserResult().setIs_answer(2);//答对了
                            }

                            if (this.resultWebSocket.getUserResult().getUser().getOpenId().equals(item.resultWebSocket.getBlueUserResult().getUser().getOpenId())) {
                                item.resultWebSocket.getUserResult().setSelectAnswer(StringUtils.isBlank(answerId) ? "" : answerId);
                                item.resultWebSocket.getBlueUserResult().setSelectAnswerOpt(answer);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt2(answer2);
                                item.resultWebSocket.getBlueUserResult().setIs_answer(2);
                            }

                            if (this.resultWebSocket.getUserResult().getUser().getOpenId().equals(item.resultWebSocket.getRedUserResult().getUser().getOpenId())) {
                                item.resultWebSocket.getUserResult().setSelectAnswer(StringUtils.isBlank(answerId) ? "" : answerId);
                                item.resultWebSocket.getRedUserResult().setSelectAnswerOpt(answer);
                                item.resultWebSocket.getUserResult().setSelectAnswerOpt2(answer2);
                                item.resultWebSocket.getRedUserResult().setIs_answer(2);
                            }

                            item.resultWebSocket.setGameState(gameStatus);
                            if (gameStatus == 3) {
                                //如果结束 需要设置一下未开始游戏 因为前端弹结算页面了，所以不需要在结束了，因为退出的时候会判断一下
                                //如果退出的时候gameStatus还是3的话 就结算，但实际是正常结束了(详情可以看 onclose方法)
                                item.isGame = false;
                            } else {
                                item.sendMessage(item.session, toJSONString(item.resultWebSocket));
                            }

                        }
                    }

                    if (gameStatus == 3) {
                        Room room = roomService.getOne(new QueryWrapper<Room>()
                                .eq("room_no", this.resultWebSocket.getRoom()));
                        room.setEndTime(new Date());
                        roomService.updateById(room);

                        UserOneVsOneLog userOneVsOneLog = userOneVsOneLogService.getOne(new QueryWrapper<UserOneVsOneLog>().eq("room_id", room.getRoomNo()));

                        if (userOneVsOneLog.getRoomOpenId().equals(this.resultWebSocket.getBlueUserResult().getUser().getOpenId())) {
                            userOneVsOneLog.setRoomOpenScore(this.resultWebSocket.getBlueUserResult().getScore());
                            userOneVsOneLog.setFriendOpenScore(this.resultWebSocket.getRedUserResult().getScore());
                            if (this.resultWebSocket.getBlueUserResult().getScore() > this.resultWebSocket.getRedUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(1);
                            } else if (this.resultWebSocket.getBlueUserResult().getScore() < this.resultWebSocket.getRedUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(2);
                            } else {
                                userOneVsOneLog.setIsWin(3);
                            }
                        } else {
                            userOneVsOneLog.setRoomOpenScore(this.resultWebSocket.getRedUserResult().getScore());
                            userOneVsOneLog.setFriendOpenScore(this.resultWebSocket.getBlueUserResult().getScore());
                            if (this.resultWebSocket.getRedUserResult().getScore() > this.resultWebSocket.getBlueUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(1);
                            } else if (this.resultWebSocket.getRedUserResult().getScore() < this.resultWebSocket.getBlueUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(2);
                            } else {
                                userOneVsOneLog.setIsWin(3);
                            }
                        }

                        userOneVsOneLogService.updateById(userOneVsOneLog);


                        int roomExperienceNum = 0;
                        int roomCoinNum = 0;
                        int friendExperienceNum = 0;
                        int friendCoinNum = 0;

                        //加积分
                        int roomOpenId = userOneVsOneLogService.getGameLogNumByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),
                                userOneVsOneLog.getRoomOpenId());
                        int friendOpenId = userOneVsOneLogService.getGameLogNumByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),
                                userOneVsOneLog.getFriendOpenId());
                        com.wangchen.entity.User roomUser = userService.getUserByOpenId(userOneVsOneLog.getRoomOpenId());
                        com.wangchen.entity.User friendUser = userService.getUserByOpenId(userOneVsOneLog.getFriendOpenId());

                        if (1 == userOneVsOneLog.getIsWin()) {//房主获胜
                            //如果今日玩的次数少于10次 就加积分 否则不加
                            if (roomOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 2, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 2;
                            } else if (roomOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 0, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 0;
                            }
                            ;

                            if (friendOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 1, 0, friendUser);
                                friendExperienceNum = 1;
                                friendCoinNum = 0;
                            }

                        } else if (2 == userOneVsOneLog.getIsWin()) {//客人获胜
                            if (roomOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 1, 0, roomUser);
                                roomExperienceNum = 1;
                                roomCoinNum = 0;
                            }

                            if (friendOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 2, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 2;
                            } else if (friendOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 0, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 0;
                            }
                            ;
                        } else {//打平
                            if (roomOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 2, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 2;
                            } else if (roomOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 0, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 0;
                            }
                            ;

                            if (friendOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 2, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 2;
                            } else if (friendOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 0, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 0;
                            }
                            ;
                        }

                        List<WebSock> webSockList = WebSock.getAllWebSock(this);
                        for (WebSock item : webSockList) {
                            if (item.resultWebSocket.getUserResult().getUser().getOpenId()
                                    .equals(userOneVsOneLog.getRoomOpenId())) {
                                item.resultWebSocket.setGetExperienceNum(roomExperienceNum);
                                item.resultWebSocket.setGetCoinNum(roomCoinNum);
                            }
                            if (item.resultWebSocket.getUserResult().getUser().getOpenId()
                                    .equals(userOneVsOneLog.getFriendOpenId())) {
                                item.resultWebSocket.setGetExperienceNum(friendExperienceNum);
                                item.resultWebSocket.setGetCoinNum(friendCoinNum);
                            }
                            item.sendMessage(item.session, toJSONString(item.resultWebSocket));
                        }
                    }
                    return;
                }

                if ("isNext".equals(type)) {
                    // 重置该房间所有用户数据
                    // 如果已经重置则跳过
                    synchronized (this) {
                        if (this.isReset) return;
                        List<WebSock> webSockList = WebSock.getAllWebSock(this);
                        for (WebSock item : webSockList) {
                            if (item == null || item.resultWebSocket == null) {
                                continue;
                            }
                            if (!item.isReset) {
                                item.resultWebSocket.getUserResult().setIs_answer(-1);
                                item.resultWebSocket.getBlueUserResult().setIs_answer(-1);
                                item.resultWebSocket.getRedUserResult().setIs_answer(-1);
                                item.resultWebSocket.setGameState(1);
                                item.resultWebSocket.getBlueUserResult().setSelectAnswer(null);
                                item.resultWebSocket.getRedUserResult().setSelectAnswer(null);
                                item.resultWebSocket.getBlueUserResult().setSelectAnswerOpt(null);
                                item.resultWebSocket.getRedUserResult().setSelectAnswerOpt(null);
                                item.isReset = true;
                                item.isTimeOut = false;
                                item.sendMessage(item.session, toJSONString(item.resultWebSocket));
                            }
                        }
                    }
                    return;
                }

                if ("timeOut".equals(type)) {
                    log.info("进入timeOut ----------------------------------------------------");
                    if (this.isTimeOut) {
                        return;
                    }
                    List<WebSock> webSockList = new ArrayList<WebSock>();
                    WebSock timeOutWebSock2 = null;//这个是真真超时的人  比如: 第一个人答题了，等第二个人超时了，第一个人也是会发送超时的
                    if (this.resultWebSocket.getUserResult().getIs_answer() == 1 ||
                            this.resultWebSocket.getUserResult().getIs_answer() == 2) {
                        webSockList.add(WebSock.getAllWebSockDelMe(this).get(0));
                    } else {
                        webSockList.add(this);
                        timeOutWebSock2 = WebSock.getAllWebSockDelMe(this).get(0);
                        if (timeOutWebSock2.resultWebSocket.getUserResult().getIs_answer() == 1 ||
                                timeOutWebSock2.resultWebSocket.getUserResult().getIs_answer() == 2) {
                        } else {
                            webSockList.add(timeOutWebSock2);
                        }
                    }

                    Integer topicId = json.getInteger("topicId");//题目id
                    int gameStatus = 1;
                    for (WebSock timeOutWebSock : webSockList) {
                        //更新答题状态
                        timeOutWebSock.resultWebSocket.getUserResult().setIs_answer(2);

                        timeOutWebSock.isTimeOut = true;
                        timeOutWebSock.isReset = false;
                        timeOutWebSock.resultWebSocket.setCode(null);


                        //如果是最后一题 那就结束
                        if (topicId.intValue() == timeOutWebSock.answerResponse.get(timeOutWebSock.answerResponse.size() - 1).getId()) {
                            gameStatus = 3;// 3是题目全部答完
                        } else {
                            gameStatus = 2;// 2是本道题答完 进行下一题
                        }

                        //把当前答题成功的人的情况告诉 别的玩家
//                        if (timeOutWebSock.resultWebSocket.getUserResult().getUser().getOpenId().equals(timeOutWebSock.resultWebSocket.getBlueUserResult().getUser().getOpenId())) {
//                            timeOutWebSock.resultWebSocket.getBlueUserResult().setIs_answer(2);
//                            timeOutWebSock.resultWebSocket.getBlueUserResult().setSelectAnswer("timeOut");
//                            timeOutWebSock.resultWebSocket.getBlueUserResult().setSelectAnswerOpt("timeOut");
//                        }
//
//                        if (timeOutWebSock.resultWebSocket.getUserResult().getUser().getOpenId().equals(timeOutWebSock.resultWebSocket.getRedUserResult().getUser().getOpenId())) {
//                            timeOutWebSock.resultWebSocket.getRedUserResult().setIs_answer(2);
//                            timeOutWebSock.resultWebSocket.getRedUserResult().setSelectAnswer("timeOut");
//                            timeOutWebSock.resultWebSocket.getRedUserResult().setSelectAnswerOpt("timeOut");
//                        }

                        /** 20220627  解决一方答题，另一方未答题超时导致答题方多加10分bug  */
                        timeOutWebSock.resultWebSocket.getBlueUserResult().setIs_answer(2);
                        timeOutWebSock.resultWebSocket.getBlueUserResult().setSelectAnswer("timeOut");
                        timeOutWebSock.resultWebSocket.getBlueUserResult().setSelectAnswerOpt("timeOut");
                        timeOutWebSock.resultWebSocket.getRedUserResult().setIs_answer(2);
                        timeOutWebSock.resultWebSocket.getRedUserResult().setSelectAnswer("timeOut");
                        timeOutWebSock.resultWebSocket.getRedUserResult().setSelectAnswerOpt("timeOut");
                        /** 20220627 */

                        if (gameStatus == 3) {
                            //如果结束 需要设置一下未开始游戏 因为前端弹结算页面了，所以不需要在结束了，因为退出的时候会判断一下
                            //如果退出的时候gameStatus还是3的话 就结算，但实际是正常结束了(详情可以看 onclose方法)
                            timeOutWebSock.isGame = false;
                        }
//                        timeOutWebSock.sendMessage(timeOutWebSock.session,toJSONString(timeOutWebSock.resultWebSocket));
                    }
                    List<WebSock> webSockList1 = WebSock.getAllWebSock(this);
                    for (WebSock webSock : webSockList1) {
                        webSock.resultWebSocket.setGameState(gameStatus);
                        if (gameStatus != 3) {
                            webSock.sendMessage(webSock.session, toJSONString(webSock.resultWebSocket));
                        }
                    }

                    int roomExperienceNum = 0;
                    int roomCoinNum = 0;
                    int friendExperienceNum = 0;
                    int friendCoinNum = 0;

                    if (gameStatus == 3) {
                        Room room = roomService.getOne(new QueryWrapper<Room>()
                                .eq("room_no", this.resultWebSocket.getRoom()));
                        room.setEndTime(new Date());
                        roomService.updateById(room);

                        UserOneVsOneLog userOneVsOneLog = userOneVsOneLogService.getOne(new QueryWrapper<UserOneVsOneLog>().eq("room_id", room.getRoomNo()));

                        int roomOpenId = userOneVsOneLogService.getGameLogNumByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),
                                userOneVsOneLog.getRoomOpenId());
                        int friendOpenId = userOneVsOneLogService.getGameLogNumByOpenId(Constants.SDF_YYYY_MM_DD.format(new Date()),
                                userOneVsOneLog.getFriendOpenId());
                        com.wangchen.entity.User roomUser = userService.getUserByOpenId(userOneVsOneLog.getRoomOpenId());
                        com.wangchen.entity.User friendUser = userService.getUserByOpenId(userOneVsOneLog.getFriendOpenId());

                        if (userOneVsOneLog.getRoomOpenId().equals(this.resultWebSocket.getBlueUserResult().getUser().getOpenId())) {
                            userOneVsOneLog.setRoomOpenScore(this.resultWebSocket.getBlueUserResult().getScore());
                            userOneVsOneLog.setFriendOpenScore(this.resultWebSocket.getRedUserResult().getScore());
                            if (this.resultWebSocket.getBlueUserResult().getScore() > this.resultWebSocket.getRedUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(1);
                            } else if (this.resultWebSocket.getBlueUserResult().getScore() < this.resultWebSocket.getRedUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(2);
                            } else {
                                userOneVsOneLog.setIsWin(3);
                            }
                        } else {
                            userOneVsOneLog.setRoomOpenScore(this.resultWebSocket.getRedUserResult().getScore());
                            userOneVsOneLog.setFriendOpenScore(this.resultWebSocket.getBlueUserResult().getScore());
                            if (this.resultWebSocket.getRedUserResult().getScore() > this.resultWebSocket.getBlueUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(1);
                            } else if (this.resultWebSocket.getRedUserResult().getScore() < this.resultWebSocket.getBlueUserResult().getScore()) {
                                userOneVsOneLog.setIsWin(2);
                            } else {
                                userOneVsOneLog.setIsWin(3);
                            }
                        }


                        //加积分
                        if (1 == userOneVsOneLog.getIsWin()) {
                            //如果今日玩的次数少于10次 就加积分 否则不加
                            if (roomOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 2, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 2;
                            } else if (roomOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 0, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 0;
                            }
                            ;

                            if (friendOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 1, 0, friendUser);
                                friendExperienceNum = 1;
                                friendCoinNum = 0;
                            }
                            ;

                        } else if (2 == userOneVsOneLog.getIsWin()) {
                            if (roomOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 1, 0, roomUser);
                                roomExperienceNum = 1;
                                roomCoinNum = 0;
                            }
                            ;

                            if (friendOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 2, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 2;
                            } else if (friendOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 0, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 0;
                            }
                            ;
                        } else {
                            if (roomOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 2, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 2;
                            } else if (roomOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getRoomOpenId(), 2, 0, roomUser);
                                roomExperienceNum = 2;
                                roomCoinNum = 0;
                            }
                            ;

                            if (friendOpenId <= 5) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 2, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 2;
                            } else if (friendOpenId <= 10) {
                                computeUtils.computeGame2(userOneVsOneLog.getFriendOpenId(), 2, 0, friendUser);
                                friendExperienceNum = 2;
                                friendCoinNum = 0;
                            }
                        }

                        for (WebSock item : webSockList1) {
                            if (item.resultWebSocket.getUserResult().getUser().getOpenId()
                                    .equals(userOneVsOneLog.getRoomOpenId())) {
                                item.resultWebSocket.setGetExperienceNum(roomExperienceNum);
                                item.resultWebSocket.setGetCoinNum(roomCoinNum);
                            }
                            if (item.resultWebSocket.getUserResult().getUser().getOpenId()
                                    .equals(userOneVsOneLog.getFriendOpenId())) {
                                item.resultWebSocket.setGetExperienceNum(friendExperienceNum);
                                item.resultWebSocket.setGetCoinNum(friendCoinNum);
                            }
                            item.sendMessage(item.session, toJSONString(item.resultWebSocket));
                        }
                    }
                    return;
                }

                if ("tiren".equals(type)) {
                    // 重置该房间所有用户数据
                    // 如果已经重置则跳过
                    synchronized (this) {
                        if (this.isGame) {
                            return;
                        }
                        String tiOpenId = json.getString("tiOpenId");//玩家回答的答案

                        List<WebSock> webSockList = WebSock.getAllWebSock(this);
                        for (WebSock item : webSockList) {
                            if (item.resultWebSocket.getUserResult().getUser().getOpenId().equals(tiOpenId)) {
                                item.resultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_OUT_ROOM.getCode());
                                item.sendMessage(item.session, toJSONString(item.resultWebSocket));
                                item.resultWebSocket.setCode(null);
                                item.onClose();
                                break;
                            }
//                            else{
//                                if(item.resultWebSocket.getBlueUserResult().getUser().getOpenId()
//                                        .equals(tiOpenId)){
//                                    item.resultWebSocket.setBlueUserResult(null);
//                                }
//                                if(item.resultWebSocket.getRedUserResult().getUser().getOpenId()
//                                        .equals(tiOpenId)){
//                                    item.resultWebSocket.setRedUserResult(null);
//                                }
//                                item.resultWebSocket.setCode(BussinessErrorMsg.PVP_ROOM_IS_OUT_ROOM.getCode());
//                                item.sendMessage(item.session,toJSONString(item.resultWebSocket));
//                            }
                        }
                    }
                    return;
                }

                log.error("未知消息类型type={}", type);
                return;
            } catch (Exception e) {
                log.error("onMessage error:{}", e.getMessage());
            }
        }
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("发生错误:message:{}\n stack:{}\n session:{}", throwable.getMessage(), throwable.getStackTrace(), session);
    }

    //   下面是自定义的一些方法
    public void sendMessage(Session session, String message) throws IOException {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.error("发送消息出错："+ e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized int getOnlineCount() {
        onlineCount = WebSock.webSocketSet.size();
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSock.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSock.onlineCount--;
    }

    public static String toJSONString(Object object) {
        return JSONObject.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 获取所有的队友(不包括自己)
     *
     * @param webSock
     * @return
     */
    public static List<WebSock> getAllWebSockDelMe(WebSock webSock) {
        //
        List<WebSock> webSocks = new ArrayList<WebSock>();
        webSocketSet.forEach(item -> {
            if (item != null && item.resultWebSocket != null) {
                //不是自己
                if (!item.resultWebSocket.getUserResult().getUser().getOpenId()
                        .equals(webSock.resultWebSocket.getUserResult().getUser().getOpenId())) {
                    if (item.resultWebSocket.getRoom() == webSock.resultWebSocket.getRoom()) {
                        webSocks.add(item);
                    }
                }
            }
        });
        return webSocks;
    }

    /**
     * 获取所有的队友(包括自己)
     *
     * @param webSock
     * @return
     */
    public static List<WebSock> getAllWebSock(WebSock webSock) {
        //
        List<WebSock> webSocks = new ArrayList<WebSock>();
        webSocketSet.forEach(item -> {
            if (item != null && item.resultWebSocket != null) {
                if (item.resultWebSocket.getRoom() == webSock.resultWebSocket.getRoom()) {
                    webSocks.add(item);
                }
            }
        });
        return webSocks;
    }

}
