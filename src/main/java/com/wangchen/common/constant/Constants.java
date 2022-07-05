package com.wangchen.common.constant;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @ProjectName: 常量类
 * @Package: com.wangchen.common.constant
 * @ClassName: Constants
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/5/20 15:40
 * @Version: 1.0
 */
public class Constants {
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 参数管理 cache name
     */
    public static final String SYS_CONFIG_CACHE = "sys-config";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache name
     */
    public static final String SYS_DICT_CACHE = "sys-dict";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";
    public final static String DIR_PATH = System.getProperty("user.dir");

    public final static String TEMP_NAME = "temp_";

    public final static SimpleDateFormat SDF_YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
    public final static SimpleDateFormat SDF_YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat SDF_YYYY_MM_DD_HH_MM_SS_CHINA = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

    // 用户成就图片地址
    public static final String ACHIEVEMENT_IMAGE_ADDRESS = "https://wxgame.jicf.net/tower_ningxia/uploadapi/upload/achievement/";

    // 项目启动使用时间
    public static final String START_USER_DAY = "2022.03.01";

    //通过部门Id获取题库信息
    public final static Map<Integer,Integer> branchTypeMap = new HashMap<Integer,Integer>();

    //通过部门Id获取题库信息（二期）
    public final static Map<Integer,Integer[]> newBranchTypeMap = new HashMap<Integer,Integer[]>();

    // 每日答题，通过分数获取经验值与塔币
    public final static Map<Integer,Integer[]> socreMap = new HashMap<Integer,Integer[]>();

    // 答题挑战，通过分数获取经验值与塔币（第二版）
    public final static Map<Integer,Integer[]> newActivitySocreMap = new HashMap<Integer,Integer[]>();

    //活动赛通过分数获取经验值与塔币
    public final static Map<Integer,Integer[]> activitySocreMap = new HashMap<Integer,Integer[]>();

    //团队赛通过分数获取经验值与塔币
    public final static Map<Integer,Integer[]> thereSocreMap = new HashMap<Integer,Integer[]>();

    //每日答题的完成天数对应成就信息
    public final static Map<Integer,Integer> dayWanCheng_ChengJiuMap = new HashMap<Integer,Integer>();

    //成就信息 对应 每日答题的完成天数
    public final static Map<Integer,Integer> chengJiu_DayWanChengMap = new HashMap<Integer,Integer>();

    //等级对应的称号
    public final static Map<Integer,Integer> LEVLE_HONOR_MAP = new HashMap<Integer,Integer>();

    // 部门
    public final static Map<Integer,String> buMenKu = new HashMap<Integer, String>();

    // 部门（二期）
    public final static Map<Integer,String[]> newBuMenKu = new HashMap<Integer, String[]>();

    // BC类公司-部门对照表（二期）
    public final static Map<String,String[]> companyBuMenKu = new HashMap<String, String[]>();

    public final static Random random = new Random();

    // 版本更新及其他原因导致停服时间区间信息
    public final static Map<Integer, String> stopServerTime = new HashMap<Integer, String>();

    // 团队赛启用时间区间信息
    public final static Map<Integer, String> startTermTime = new HashMap<Integer, String>();

    // 人员登录次数
    public static int COUNT;

    static{

        COUNT = 0;

        //key对应部门id value数组表示部门对应题库集合(二期)
//        newBranchTypeMap.put(1,new Integer[]{1,0});
//        newBranchTypeMap.put(2,new Integer[]{1,5});
//        newBranchTypeMap.put(3,new Integer[]{1,7});
//        newBranchTypeMap.put(4,new Integer[]{1,3});
//        newBranchTypeMap.put(5,new Integer[]{1,7});
//        newBranchTypeMap.put(6,new Integer[]{1,2});
//        newBranchTypeMap.put(7,new Integer[]{1,4});
//        newBranchTypeMap.put(8,new Integer[]{1,6});
//        newBranchTypeMap.put(9,new Integer[]{1,5});
//        newBranchTypeMap.put(10,new Integer[]{1,1});
//        newBranchTypeMap.put(11,new Integer[]{1,2});
//        newBranchTypeMap.put(12,new Integer[]{1,1});
//        newBranchTypeMap.put(13,new Integer[]{1,2});
//        newBranchTypeMap.put(14,new Integer[]{1,8});

        //key对应部门id value数组表示部门对应题库集合(宁夏)
        newBranchTypeMap.put(1,new Integer[]{1,0});
        newBranchTypeMap.put(2,new Integer[]{1,1});
        newBranchTypeMap.put(3,new Integer[]{1,2});
        newBranchTypeMap.put(4,new Integer[]{1,3});
        newBranchTypeMap.put(5,new Integer[]{1,4});
        newBranchTypeMap.put(6,new Integer[]{1,5});
        newBranchTypeMap.put(7,new Integer[]{1,6});
        newBranchTypeMap.put(8,new Integer[]{1,7});
        newBranchTypeMap.put(9,new Integer[]{1,8});
        newBranchTypeMap.put(10,new Integer[]{1,9});
        newBranchTypeMap.put(11,new Integer[]{1,10});
        newBranchTypeMap.put(12,new Integer[]{1,11});

        //key是分数  value的[0]是经验值 value[1]是塔币值
        socreMap.put(0,new Integer[]{1,0});
        socreMap.put(10,new Integer[]{1,0});
        socreMap.put(20,new Integer[]{1,0});
        socreMap.put(30,new Integer[]{1,0});
        socreMap.put(40,new Integer[]{1,0});
        socreMap.put(50,new Integer[]{1,0});
        socreMap.put(60,new Integer[]{3,3});
        socreMap.put(70,new Integer[]{3,3});
        socreMap.put(80,new Integer[]{3,3});
        socreMap.put(90,new Integer[]{3,3});
        socreMap.put(100,new Integer[]{5,5});

        // key是分数 value的[0]是经验值 value[1]是塔币值
        newActivitySocreMap.put(0,new Integer[]{0,0});
        newActivitySocreMap.put(10,new Integer[]{2,1});
        newActivitySocreMap.put(20,new Integer[]{2,1});
        newActivitySocreMap.put(30,new Integer[]{2,1});
        newActivitySocreMap.put(40,new Integer[]{2,1});
        newActivitySocreMap.put(50,new Integer[]{2,1});
        newActivitySocreMap.put(60,new Integer[]{5,3});
        newActivitySocreMap.put(70,new Integer[]{5,3});
        newActivitySocreMap.put(80,new Integer[]{5,3});
        newActivitySocreMap.put(90,new Integer[]{5,3});
        newActivitySocreMap.put(100,new Integer[]{10,5});

        //key是名次 value是活动赛名称对应的经验和塔币  key =0是值排名10以外的所有选手值
        activitySocreMap.put(1,new Integer[]{20,20});
        activitySocreMap.put(2,new Integer[]{19,19});
        activitySocreMap.put(3,new Integer[]{18,18});
        activitySocreMap.put(4,new Integer[]{17,17});
        activitySocreMap.put(5,new Integer[]{16,16});
        activitySocreMap.put(6,new Integer[]{15,15});
        activitySocreMap.put(7,new Integer[]{14,14});
        activitySocreMap.put(8,new Integer[]{13,13});
        activitySocreMap.put(9,new Integer[]{12,12});
        activitySocreMap.put(10,new Integer[]{11,11});
        activitySocreMap.put(0,new Integer[]{5,5});

        //key是名次 value是活动赛名称对应的经验和塔币  key =0是值排名10以外的所有选手值
        thereSocreMap.put(1,new Integer[]{10,5});
        thereSocreMap.put(2,new Integer[]{8,3});
        thereSocreMap.put(3,new Integer[]{5,1});
        thereSocreMap.put(0,new Integer[]{1,0});

        //key是天数 value是对应成就表里的id
        dayWanCheng_ChengJiuMap.put(7,1);
        dayWanCheng_ChengJiuMap.put(30,2);
        dayWanCheng_ChengJiuMap.put(90,3);
        dayWanCheng_ChengJiuMap.put(180,4);
        dayWanCheng_ChengJiuMap.put(365,5);

        //key是对应成就表里的id value是天数
        chengJiu_DayWanChengMap.put(1,7);
        chengJiu_DayWanChengMap.put(2,30);
        chengJiu_DayWanChengMap.put(3,90);
        chengJiu_DayWanChengMap.put(4,180);
        chengJiu_DayWanChengMap.put(5,365);

        //key是部门id，value是部门名称 (二期)
//        newBuMenKu.put(1,new String[]{"1","通信发展"});
//        newBuMenKu.put(2,new String[]{"1","运营维护"});
//        newBuMenKu.put(3,new String[]{"1","行业拓展"});
//        newBuMenKu.put(4,new String[]{"1","能源经营"});
//        newBuMenKu.put(5,new String[]{"1","财务管理"});
//        newBuMenKu.put(6,new String[]{"1","人力资源"});
//        newBuMenKu.put(7,new String[]{"1","党群纪检"});
//        newBuMenKu.put(8,new String[]{"1","综合管理"});
//
//        newBuMenKu.put(101,new String[]{"2","代维"});
//
//        newBuMenKu.put(201,new String[]{"3","地勘"});
//        newBuMenKu.put(202,new String[]{"3","监理"});
//        newBuMenKu.put(203,new String[]{"3","设计"});
//        newBuMenKu.put(204,new String[]{"3","施工"});
//        newBuMenKu.put(205,new String[]{"3","土建"});
//        newBuMenKu.put(206,new String[]{"3","电力"});
//        newBuMenKu.put(207,new String[]{"3","机房"});
//        newBuMenKu.put(208,new String[]{"3","动力配套"});
//        newBuMenKu.put(209,new String[]{"3","电池管理"});
//        newBuMenKu.put(210,new String[]{"3","天线"});
//        newBuMenKu.put(211,new String[]{"3","门禁"});
//        newBuMenKu.put(212,new String[]{"3","暂无分类"});

        //key是部门id，value是部门名称 (二期)
//        newBuMenKu.put(1,new String[]{"1","通信发展"});
//        newBuMenKu.put(2,new String[]{"1","行业拓展"});
//        newBuMenKu.put(3,new String[]{"1","能源经营"});
//        newBuMenKu.put(4,new String[]{"1","运营维护"});
//        newBuMenKu.put(5,new String[]{"1","综合"});
//        newBuMenKu.put(6,new String[]{"1","财务"});
//        newBuMenKu.put(7,new String[]{"1","人力资源"});
//        newBuMenKu.put(8,new String[]{"1","纪委办公室"});
//        newBuMenKu.put(9,new String[]{"1","党群工作部"});
//        newBuMenKu.put(10,new String[]{"1","商务合作中心"});
//        newBuMenKu.put(11,new String[]{"1","技术支撑中心"});

        newBuMenKu.put(1,new String[]{"1","通信发展"});
        newBuMenKu.put(2,new String[]{"1","行业拓展"});
        newBuMenKu.put(3,new String[]{"1","能源经营"});
        newBuMenKu.put(4,new String[]{"1","运营维护"});
        newBuMenKu.put(5,new String[]{"1","综合管理"});
        newBuMenKu.put(6,new String[]{"1","财务管理"});
        newBuMenKu.put(7,new String[]{"1","人力资源"});
        newBuMenKu.put(8,new String[]{"1","纪委办公室"});
        newBuMenKu.put(9,new String[]{"1","党群工作部"});
        newBuMenKu.put(10,new String[]{"1","商合管理"});
        newBuMenKu.put(11,new String[]{"1","技术支撑中心"});

        //key是等级 value是对应称号表里的id
        LEVLE_HONOR_MAP.put(10,1);
        LEVLE_HONOR_MAP.put(20,2);
        LEVLE_HONOR_MAP.put(30,3);
        LEVLE_HONOR_MAP.put(40,4);
        LEVLE_HONOR_MAP.put(50,5);
        LEVLE_HONOR_MAP.put(60,6);
        LEVLE_HONOR_MAP.put(70,7);
        LEVLE_HONOR_MAP.put(80,8);
        LEVLE_HONOR_MAP.put(90,9);
        LEVLE_HONOR_MAP.put(100,10);

        // BC类公司-部门对照表（二期）
//        companyBuMenKu.put("云南钰成",new String[] {"代维"});
//        companyBuMenKu.put("广东长实",new String[] {"代维"});
//        companyBuMenKu.put("赛尔通信",new String[] {"代维"});
//        companyBuMenKu.put("云南铁通",new String[] {"代维"});
//        companyBuMenKu.put("云南通服",new String[] {"代维"});
//        companyBuMenKu.put("云南鸿拓",new String[] {"代维"});
//        companyBuMenKu.put("润建股份",new String[] {"代维"});
//        companyBuMenKu.put("成都军通",new String[] {"代维"});
//        companyBuMenKu.put("山东邮电",new String[] {"代维"});
//        companyBuMenKu.put("盛毅达",new String[] {"代维"});
//        companyBuMenKu.put("云南国豪",new String[] {"代维"});
//        companyBuMenKu.put("广东长高",new String[] {"代维"});
//
//        companyBuMenKu.put("北京城建",new String[] {"地勘"});
//        companyBuMenKu.put("明达海洋",new String[] {"地勘"});
//        companyBuMenKu.put("西南有色",new String[] {"地勘"});
//        companyBuMenKu.put("中通服项目管理",new String[] {"监理"});
//        companyBuMenKu.put("云南奋进",new String[] {"监理"});
//        companyBuMenKu.put("广州汇源",new String[] {"监理"});
//        companyBuMenKu.put("公诚管理",new String[] {"监理"});
//        companyBuMenKu.put("北京驰跃翔",new String[] {"监理"});
//        companyBuMenKu.put("深圳都信",new String[] {"监理"});
//        companyBuMenKu.put("四川公众",new String[] {"监理"});
//        companyBuMenKu.put("福建邮电设计院",new String[] {"设计"});
//        companyBuMenKu.put("广东电信设计院",new String[] {"设计"});
//        companyBuMenKu.put("重庆信科",new String[] {"设计"});
//        companyBuMenKu.put("中讯邮电设计院",new String[] {"设计"});
//        companyBuMenKu.put("中通服咨询设计",new String[] {"设计"});
//        companyBuMenKu.put("四川通信科研",new String[] {"设计"});
//        companyBuMenKu.put("湖南邮电设计院",new String[] {"设计"});
//        companyBuMenKu.put("云南振华邮电",new String[] {"暂无分类"});
//        companyBuMenKu.put("华通誉球",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南隆翔铁塔",new String[] {"暂无分类"});
//        companyBuMenKu.put("河北鼎盛塔业",new String[] {"暂无分类"});
//        companyBuMenKu.put("中邮科通信",new String[] {"暂无分类"});
//        companyBuMenKu.put("衡水天翔通讯",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南中林",new String[] {"暂无分类"});
//        companyBuMenKu.put("四川航天天盛",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南省设计院",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南电网",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南桑尔特",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南中江机电",new String[] {"暂无分类"});
//        companyBuMenKu.put("临沧奋进",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南金戈",new String[] {"暂无分类"});
//        companyBuMenKu.put("临沧铭晟",new String[] {"暂无分类"});
//        companyBuMenKu.put("临沧南屏建筑",new String[] {"暂无分类"});
//        companyBuMenKu.put("中移建设云南",new String[] {"暂无分类"});
//        companyBuMenKu.put("厦门爱维达",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南杰岭科技",new String[] {"暂无分类"});
//        companyBuMenKu.put("临沧盛林通信",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南双九",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南振华技术",new String[] {"暂无分类"});
//        companyBuMenKu.put("元江热地亚",new String[] {"暂无分类"});
//        companyBuMenKu.put("成都德芯数字",new String[] {"暂无分类"});
//        companyBuMenKu.put("云南典大建设",new String[] {"暂无分类"});
//        companyBuMenKu.put("中铁二院",new String[] {"设计"});
//        companyBuMenKu.put("云南铁路工程",new String[] {"施工"});
//        companyBuMenKu.put("通号工程局",new String[] {"施工"});
//        companyBuMenKu.put("中铁电气",new String[] {"施工"});
//        companyBuMenKu.put("中铁武汉电气",new String[] {"施工"});
//        companyBuMenKu.put("中国铁路通信",new String[] {"施工"});
//        companyBuMenKu.put("武汉虹信",new String[] {"施工"});
//        companyBuMenKu.put("天津京信",new String[] {"施工"});
//        companyBuMenKu.put("华为技术服务",new String[] {"施工"});
//        companyBuMenKu.put("四川景云祥",new String[] {"土建"});
//        companyBuMenKu.put("云南东浩",new String[] {"土建"});
//        companyBuMenKu.put("中通服建设",new String[] {"土建"});
//        companyBuMenKu.put("云南兴黄建筑",new String[] {"土建"});
//        companyBuMenKu.put("中徽建技术",new String[] {"土建"});
//        companyBuMenKu.put("广西正地建设",new String[] {"土建"});
//        companyBuMenKu.put("云南明岭",new String[] {"土建"});
//        companyBuMenKu.put("云南东申通信",new String[] {"土建"});
//        companyBuMenKu.put("昆明晋宁建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南华田建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南德凯通信",new String[] {"土建"});
//        companyBuMenKu.put("宣威大亚建工",new String[] {"土建"});
//        companyBuMenKu.put("弥勒弥阳建筑",new String[] {"土建"});
//        companyBuMenKu.put("昆明润兴",new String[] {"土建"});
//        companyBuMenKu.put("安徽广达",new String[] {"土建"});
//        companyBuMenKu.put("大理第十二建筑",new String[] {"土建"});
//        companyBuMenKu.put("四川汇源",new String[] {"土建"});
//        companyBuMenKu.put("红河明丰建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南恒际建筑",new String[] {"土建"});
//        companyBuMenKu.put("昭通建筑工程",new String[] {"土建"});
//        companyBuMenKu.put("镇雄集英建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南通成",new String[] {"土建"});
//        companyBuMenKu.put("四川广安智丰",new String[] {"土建"});
//        companyBuMenKu.put("云南喜洲建设",new String[] {"土建"});
//        companyBuMenKu.put("中动信息产业",new String[] {"土建"});
//        companyBuMenKu.put("大理通泰建筑",new String[] {"土建"});
//        companyBuMenKu.put("昆明杨林建工",new String[] {"土建"});
//        companyBuMenKu.put("赣通通信",new String[] {"土建"});
//        companyBuMenKu.put("云南辛盛水电",new String[] {"土建"});
//        companyBuMenKu.put("文山宏强建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南邮电工程",new String[] {"土建"});
//        companyBuMenKu.put("新平新乡建筑",new String[] {"土建"});
//        companyBuMenKu.put("湖南天辰建设",new String[] {"土建"});
//        companyBuMenKu.put("景谷方圆建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南新基点",new String[] {"土建"});
//        companyBuMenKu.put("海纳通讯技术",new String[] {"土建"});
//        companyBuMenKu.put("云南保山地建建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南保山宏勘建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南景顺建设",new String[] {"土建"});
//        companyBuMenKu.put("永胜兴文建筑",new String[] {"土建"});
//        companyBuMenKu.put("凤庆鸿发建筑",new String[] {"土建"});
//        companyBuMenKu.put("景洪第二建筑",new String[] {"土建"});
//        companyBuMenKu.put("云南祥泰建筑",new String[] {"土建"});
//        companyBuMenKu.put("勐海诚鑫建筑",new String[] {"土建"});
//        companyBuMenKu.put("昆明科泰通信",new String[] {"土建"});
//        companyBuMenKu.put("云南兴宏建筑",new String[] {"土建"});
//        companyBuMenKu.put("昆明明利丰",new String[] {"土建"});
//        companyBuMenKu.put("浙江邮电工程",new String[] {"土建"});
//        companyBuMenKu.put("玉龙恒杰建筑",new String[] {"土建"});
//        companyBuMenKu.put("丽江天雨水电",new String[] {"土建"});
//        companyBuMenKu.put("中邮建技术",new String[] {"土建"});
//        companyBuMenKu.put("云南泓振祥建筑",new String[] {"土建"});
//        companyBuMenKu.put("怒江明谷建筑",new String[] {"土建"});
//        companyBuMenKu.put("泸水新城建筑",new String[] {"土建"});
//        companyBuMenKu.put("湖南明宇宏乔电力",new String[] {"电力"});
//        companyBuMenKu.put("黑龙江电信国脉",new String[] {"电力"});
//        companyBuMenKu.put("云南元象电力",new String[] {"电力"});
//        companyBuMenKu.put("云南恒联建筑",new String[] {"电力"});
//        companyBuMenKu.put("云南宏源电力",new String[] {"电力"});
//        companyBuMenKu.put("四川省送变电建设",new String[] {"电力"});
//        companyBuMenKu.put("云南昊滇建设",new String[] {"电力"});
//        companyBuMenKu.put("云南良和实业",new String[] {"电力"});
//        companyBuMenKu.put("云南万泽电力",new String[] {"电力"});
//        companyBuMenKu.put("曲靖凌云工程",new String[] {"电力"});
//        companyBuMenKu.put("贵州中凯隆",new String[] {"电力"});
//        companyBuMenKu.put("云南昊昕达电力",new String[] {"电力"});
//        companyBuMenKu.put("云南宝光电力",new String[] {"电力"});
//        companyBuMenKu.put("云南傲鹏电力",new String[] {"电力"});
//        companyBuMenKu.put("云南国盛送变电",new String[] {"电力"});
//        companyBuMenKu.put("四川省升辉建筑",new String[] {"电力"});
//        companyBuMenKu.put("四川斌佳电力",new String[] {"电力"});
//        companyBuMenKu.put("云南上侑电力",new String[] {"电力"});
//        companyBuMenKu.put("中全通技术",new String[] {"电力"});
//        companyBuMenKu.put("河南省富臣电力",new String[] {"电力"});
//        companyBuMenKu.put("云南亿安水利水电",new String[] {"电力"});
//        companyBuMenKu.put("广西象新通信",new String[] {"电力"});
//        companyBuMenKu.put("云南康博电力",new String[] {"电力"});
//        companyBuMenKu.put("云南华远电力",new String[] {"电力"});
//        companyBuMenKu.put("云南海中金电力",new String[] {"电力"});
//        companyBuMenKu.put("河南省盛发电力",new String[] {"电力"});
//        companyBuMenKu.put("四川省津华电力",new String[] {"电力"});
//        companyBuMenKu.put("云南常瑞建设",new String[] {"电力"});
//        companyBuMenKu.put("东亚电力",new String[] {"电力"});
//        companyBuMenKu.put("云南鸿昇电力",new String[] {"电力"});
//        companyBuMenKu.put("云南嘉浩电力",new String[] {"电力"});
//        companyBuMenKu.put("云南网能建设",new String[] {"电力"});
//        companyBuMenKu.put("中国通信建设北京局",new String[] {"电力"});
//        companyBuMenKu.put("云南腾晟电力",new String[] {"电力"});
//        companyBuMenKu.put("河南中能建设",new String[] {"电力"});
//        companyBuMenKu.put("怒江庆鑫源实业",new String[] {"电力"});
//        companyBuMenKu.put("厦门纵横集团",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("云南万向通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("上海大唐移动通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("中移建设",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("湖南通信建设",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("重庆信科通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("深圳国人通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("云南力捷通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("贝优特技术",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("北京中福通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("广东南方通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("昆明华兴达通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("河南通信工程局",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("中国通信建设第二工程局",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("绵阳恒盛通信",new String[] {"机房","动力配套"});
//        companyBuMenKu.put("江苏亚奥科",new String[] {"电池管理"});
//        companyBuMenKu.put("北京瑞祺皓迪",new String[] {"电池管理"});
//        companyBuMenKu.put("安徽省金屹电源",new String[] {"电池管理"});
//        companyBuMenKu.put("摩比天线",new String[] {"天线"});
//        companyBuMenKu.put("上海汇珏网络",new String[] {"天线"});
//        companyBuMenKu.put("吉林施泰",new String[] {"门禁"});
//        companyBuMenKu.put("上海中兴易联",new String[] {"门禁"});
//        companyBuMenKu.put("浙江创力电子",new String[] {"门禁"});
//        companyBuMenKu.put("河北北方伟业",new String[] {"暂无分类"});
//        companyBuMenKu.put("日海智能",new String[] {"暂无分类"});
//        companyBuMenKu.put("东莞一信实业",new String[] {"机房"});
//        companyBuMenKu.put("广东海悟科技",new String[] {"机房"});

//        stopServerTime.put(1,"2021-09-27~2021-10-06");

        startTermTime.put(1,"2022-03-01");
    }

}