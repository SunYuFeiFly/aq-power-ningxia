package com.wangchen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangchen.common.constant.Constants;
import com.wangchen.entity.*;
import com.wangchen.service.*;
import com.wangchen.utils.DateUtils;
import com.wangchen.utils.UserLevelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

/**
 * 发放前一天的3v3的奖励信息
 * @ProjectName: aq-power
 * @Package: com.wangchen
 * @Description: zhangcheng
 * @Date: 2020/7/2 16:10
 * @Version: 1.0
 */
@Slf4j                  // 日志
@Configuration          // 配置类
@EnableScheduling       // 支持定时任务类
public class ThreeRankTask {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private HotLogService hotLogService;

    @Autowired
    private HonorService honorService;

    @Autowired
    private UserHonorService userHonorService;

    @Autowired
    private AlertTipsService alertTipsService;

    @Autowired
    private ThreeRoomService threeRoomService;

    @Autowired
    private UserThreeTeamLogService userThreeTeamLogService;

    @Autowired
    private UserThreeTeamRankService userThreeTeamRankService;

    /**
     * 发放前一天的团队赛的奖励信息
     */
//    @Scheduled(cron = "0 40 0 * * ? ")
//    @Scheduled(cron = "*/5 * * * * ?")
    public void taskActivity() {
        try{

            List<ThreeRoom> threeRoomList = threeRoomService.list(new QueryWrapper<ThreeRoom>()
                    .eq("create_date",DateUtils.getZuoTianDay())
                    .eq("is_open",1).orderByDesc("score").orderByAsc("end_time"));
            if(CollectionUtils.isEmpty(threeRoomList)){
                return;
            }

            Map<Integer,List<ThreeRoom>> map = new HashMap<Integer,List<ThreeRoom>>();

            List<Integer> scoreList = new ArrayList<Integer>();

            for(ThreeRoom threeRoom : threeRoomList){
                if(!scoreList.contains(threeRoom.getScore())){
                    scoreList.add(threeRoom.getScore());
                }

                if(!map.containsKey(threeRoom.getScore())){
                    List<ThreeRoom> addThreeRoomList = new ArrayList<ThreeRoom>();
                    addThreeRoomList.add(threeRoom);
                    map.put(threeRoom.getScore(),addThreeRoomList);
                }else{
                    map.get(threeRoom.getScore()).add(threeRoom);
                    map.put(threeRoom.getScore(),map.get(threeRoom.getScore()));
                }
            }

            for(int i = 1; i<=scoreList.size(); i++){
                List<ThreeRoom> threeRoomListRank = map.get(scoreList.get(i-1));
                //获取分数对应的经验值和塔币
                Integer[] jiangli = null;
                if(i < 4){
                    jiangli = Constants.thereSocreMap.get(i);
                }else{
                    jiangli = Constants.thereSocreMap.get(0);
                }

                for(ThreeRoom threeRoom : threeRoomListRank){
                    UserThreeTeamRank userThreeTeamRank = new UserThreeTeamRank();
                    userThreeTeamRank.setRankDate(Constants.SDF_YYYY_MM_DD.parse(DateUtils.getZuoTianDay()));
                    userThreeTeamRank.setRoomId(threeRoom.getRoomNo().intValue());
                    userThreeTeamRank.setScore(threeRoom.getScore().intValue());
                    userThreeTeamRank.setRankNo(i);
                    userThreeTeamRank.setCreateTime(new Date());

                    StringBuffer context = new StringBuffer();
                    context.append("{");
                    List<UserThreeTeamLog> userThreeTeamLogList = userThreeTeamLogService.list(new QueryWrapper<UserThreeTeamLog>().eq("room_id",threeRoom.getId()));

                    int num = 1;
                    for(UserThreeTeamLog userThreeTeamLog : userThreeTeamLogList){
                        User user = userService.getUserByOpenId(userThreeTeamLog.getOpenId());

                        UserLevel userLevel = userLevelService.getOne(new QueryWrapper<UserLevel>().eq("open_id",userThreeTeamLog.getOpenId()));
                        //用户当前等级经验峰值
                        Integer value = UserLevelUtils.getLevelMap(userLevel.getLevelId()+1);


                        context.append("\"name"+num+"\":\""+user.getName()+"\",");
                        context.append("\"avatar"+num+"\":\""+user.getAvatar()+"\",");
                        context.append("\"level"+num+"\":\""+(userLevel.getLevelId()-1)+"\",");
                        num++;

                        //当前分数加本次应该加的分数还小于本等级的峰值， 那就不用升级
                        if(value > (user.getAllExperience().intValue()+jiangli[0].intValue())){

                            //经验值增加
                            userLevel.setUpdateTime(new Date());
                            userLevelService.updateById(userLevel);

                            //塔币增加
//                            user.setAllExperience(user.getAllExperience()+jiangli[0].intValue());
                            user.addAllExperience(jiangli[0].intValue());
                            user.setAllCoin(user.getAllCoin()+jiangli[1].intValue());
                            userService.updateById(user);

                        }else{

                            if(userLevel.getLevelId().intValue() != 121){
                                //塔币增加
                                user.setAllCoin(user.getAllCoin()+jiangli[1].intValue());
                                //总经验增加
//                                user.setAllExperience(user.getAllExperience() + jiangli[0].intValue());
                                user.addAllExperience(jiangli[0].intValue());
                                userService.updateById(user);

                                //先更改等级
                                userLevel.setLevelId(userLevel.getLevelId()+1);
                                userLevel.setLevelName((userLevel.getLevelId()-1)+"级");
                                userLevel.setNowExperience(0);
                                userLevel.setUpdateTime(new Date());
                                userLevelService.updateById(userLevel);

                                //每日公告上面的恭喜
                                HotLog hotLog = new HotLog();
                                hotLog.setOpenId(userThreeTeamLog.getOpenId());
                                hotLog.setRemarks("恭喜"+user.getName()+"升到"+userLevel.getLevelName());
                                hotLog.setCreateDate(new Date());
                                hotLogService.save(hotLog);

                                Integer honorId = Constants.LEVLE_HONOR_MAP.get(userLevel.getLevelId()-1);
                                //可获得称号
                                if(null != honorId){
                                    //添加称号信息
                                    Honor honor = honorService.getOne(new QueryWrapper<Honor>().eq("id",honorId));
                                    UserHonor userHonor = new UserHonor();
                                    userHonor.setHonorId(honorId);
                                    userHonor.setHonorName(honor.getName());
                                    userHonor.setOpenId(userThreeTeamLog.getOpenId());
                                    userHonor.setCreateTime(new Date());
                                    userHonorService.save(userHonor);

                                    //每日公告上面的恭喜
                                    HotLog hotLog1 = new HotLog();
                                    hotLog1.setOpenId(userThreeTeamLog.getOpenId());
                                    hotLog1.setRemarks("恭喜"+user.getName()+"获得"+honor.getName()+"称号");
                                    hotLog1.setCreateDate(new Date());
                                    hotLogService.save(hotLog1);

                                    //弹框提示
                                    AlertTips alertTips = new AlertTips();
                                    alertTips.setOpenId(userThreeTeamLog.getOpenId());
                                    alertTips.setHonorId(honor.getId());
                                    alertTips.setHonorName(honor.getName());
                                    alertTips.setType(0);
                                    alertTips.setStatus(0);
                                    alertTips.setCreateTime(new Date());
                                    alertTipsService.save(alertTips);
                                }
                            }else{
                                //塔币增加
                                user.setAllCoin(user.getAllCoin()+jiangli[1].intValue());
//                                user.setAllExperience(user.getAllExperience() + jiangli[0].intValue());
                                user.addAllExperience(jiangli[0].intValue());
                                userService.updateById(user);

                                userLevel.setUpdateTime(new Date());
                                userLevelService.updateById(userLevel);
                            }
                        }
                    }
                    String context1 = context.toString().substring(0,context.length()-1)+"}";
                    userThreeTeamRank.setContext(context1);
                    userThreeTeamRankService.save(userThreeTeamRank);
                }

            }

        }catch (Exception e){
            log.error("发放前一天的团队赛的奖励信息、 错误信息: {}",e);
        }

    }

}