package com.wangchen.utils;

import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author yinguang
 * @create 2020/7/3
 * @effect
 */
public class RandomTopicUtils {

    /**
     * 随机从集合里取十个不重复的数据放入新的集合里
     * @param
     * @return:
     * @Author:yinguang
     * @Date:11:05 2020/7/3
     */
    public static List topicList(List<T> list) {
        Random random = new Random();
        List lists = new ArrayList();
        while (lists.size() != 10) {
            //随机获取一个数
            int num = random.nextInt(list.size());
            //实现不重复，如果这个list集合中没有这个数就将这个数存储到list2中
            if (!lists.contains(num)) {
                lists.add(num);
            }
        }
        List result = new ArrayList();
        Iterator<Integer> iterator = lists.iterator();
        while (iterator.hasNext()) {
            result.add(list.get(iterator.next()));
        }
        return result;
    }
}
