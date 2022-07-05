package com.wangchen.utils;/**
 * @ProjectName: aq-power
 * @Package: com.wangchen.utils
 * @ClassName: CompanyUtils
 * @Author: 2
 * @Description: ${description}
 * @Date: 2020/6/2 14:25
 * @Version: 1.0
 */

import com.wangchen.entity.Branch;
import com.wangchen.entity.Company;
import com.wangchen.entity.Level;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 全部等级储存
 * @ author cheng.zhang
 * @ create 2020/6/2
 *
 */
@Slf4j
@Component
public class UserLevelUtils {

    public static Map<Integer,Integer> levelMap = new HashMap<>();

    public static Lock lock = new ReentrantLock();

    public static Integer getLevelMap(Integer level){

        try {
            lock.lock();
            if(levelMap.containsKey(level)){
                return levelMap.get(level);
            }
            return null;
        }catch (Exception e){
            log.error("获取等级信息失败");
            return null;
        }finally {
//            if(lock.tryLock()){
                lock.unlock();
//            }
        }
    }

    public static void setLevelMap(List<Level> levelList){
        try {
            lock.lock();
            for(Level level:levelList){
                levelMap.put(level.getId(),level.getValue());
            }
        }catch (Exception e){
            log.error("存放等级信息失败");
        }finally {
//            if(lock.tryLock()){
                lock.unlock();
//            }
        }
    }


}