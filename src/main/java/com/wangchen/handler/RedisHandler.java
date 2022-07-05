package com.wangchen.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 工具类
 * @Auth: huangyang
 * @Date: 2019/8/15 19:20
 */
@Component
@Slf4j
public class RedisHandler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 指定缓存失效时间
     * @param key
     * @param time 单位(s)
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 19:23
     */
    public boolean expire(String key, long time){
        try {
            if (time > 0){
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            log.error("redis expire error:{}", e);
            return false;
        }
    }

    /**
     * 根据key获取过期时间
     * @param key
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 19:27
     */
    public long getExpire(String key){
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     * @param key
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 19:28
     */
    public boolean hasKey(String key){
        try {
            return redisTemplate.hasKey(key);
        }catch (Exception e){
            log.error("redis hasKey error:{}", e);
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key
     * @Return: void
     * @Auth: huangyang
     * @Date: 2019/8/15 19:41
     */
    public void delete(String... key){
        if (key != null && key.length > 0){
            if (key.length == 1){
                redisTemplate.delete(key[0]);
            }else{
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    /**
     * 获取缓存
     * @param key
     * @Return: java.lang.Object
     * @Auth: huangyang
     * @Date: 2019/8/15 19:43
     */
    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存 hash get
     * @param key
     * @param item
     * @Return: java.lang.Object
     * @Auth: huangyang
     * @Date: 2019/8/15 19:54
     */
    public Object get(String key, String item){
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 设置缓存
     * @param key
     * @param value
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 19:45
     */
    public boolean set(String key, Object value){
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        }catch (Exception e){
            log.error("redis set error:{}", e);
            return false;
        }
    }

    /**
     * 设置缓存
     * @param key
     * @param value
     * @param time 有效时间 单位(s)
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 19:46
     */
    public boolean set(String key, Object value, long time){
        try {
            redisTemplate.opsForValue().set(key, value, time);
            return true;
        }catch (Exception e){
            log.error("redis set error:{}", e);
            return false;
        }
    }

    /**
     * 递减
     * @param key
     * @param data
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 19:52
     */
    public long increment(String key, long data){
        if (data <= 0){
            throw new RuntimeException(String.format("递减因子必须大于0, 传入因子:%s", data));
        }
        return redisTemplate.opsForValue().increment(key, data);
    }

    /**
     * 获取hashKey对应所有键值
     * @param key
     * @Return: java.util.Map<java.lang.Object,java.lang.Object>
     * @Auth: huangyang
     * @Date: 2019/8/15 19:55
     */
    public Map<Object, Object> entries(String key){
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     * @param key
     * @param map
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:10
     */
    public boolean putAll(String key, Map<Object, Object> map){
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        }catch (Exception e){
            log.error("redis putAll error:{}", e);
            return false;
        }
    }

    /**
     * HashSet
     * @param key
     * @param map
     * @param time 有效时间:单位(s)
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:12
     */
    public boolean putAll(String key, Map<Object, Object> map, long time){
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            log.error("redis putAll error:{}", e);
            return false;
        }
    }

    /**
     * 向一张hash表存入数据,如果不存在将创建
     * @param key 键
     * @param item
     * @param value 值
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:14
     */
    public boolean put(String key, String item, Object value){
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        }catch (Exception e){
            log.error("redis put error:{}", e);
            return false;
        }
    }

    /**
     * 向一张hash表存入数据,如果不存在将创建
     * @param key
     * @param item
     * @param value
     * @param time 有效时间 单位(s) 注意:如果已存在的hash表有时间,将会替换原有时间
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:16
     */
    public boolean put(String key, String item, Object value, long time){
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            log.error("redis put error:{}", e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key
     * @param item
     * @Return: void
     * @Auth: huangyang
     * @Date: 2019/8/15 20:19
     */
    public void delete(String key, Object... item){
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项值
     * @param key
     * @param item
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:21
     */
    public boolean hasKey(String key, String item){
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增/递减 如果不存在,将创建一个并把新增的值返回
     * @param key
     * @param item
     * @param by 增加量(大于0递增 小于0递减)
     * @Return: double
     * @Auth: huangyang
     * @Date: 2019/8/15 20:22
     */
    public double increment(String key, String item, double by){
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * 根据key获取Set中的所有值
     * @param key
     * @Return: java.util.Set<java.lang.Object>
     * @Auth: huangyang
     * @Date: 2019/8/15 20:26
     */
    public Set<Object> members(String key){
        try {
            return redisTemplate.opsForSet().members(key);
        }catch (Exception e){
            log.error("redis members error:{}", e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询是否存在
     * @param key
     * @param value
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:28
     */
    public boolean isMember(String key, Object value){
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        }catch (Exception e){
            log.error("redis isMember error:{}", e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     * @param key
     * @param values
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 20:29
     */
    public long add(String key, Object... values){
        try {
            return redisTemplate.opsForSet().add(key,values);
        }catch (Exception e){
            log.error("redis add error:{}", e);
            return 0;
        }
    }

    /**
     * 将数据放入set缓存
     * @param key
     * @param time 有效时间 单位(s)
     * @param values
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 20:33
     */
    public long add(String key, long time, Object... values){
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0){
                expire(key, time);
            }
            return count;
        }catch (Exception e){
            log.error("redis add error:{}", e);
            return 0;
        }

    }

    /**
     * 获取set缓存长度
     * @param key
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 20:35
     */
    public long sizeOfSet(String key){
        try {
            return redisTemplate.opsForSet().size(key);
        }catch (Exception e){
            log.error("redis size error:{}", e);
            return 0;
        }
    }

    /**
     * set缓存移除
     * @param key
     * @param values
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 20:35
     */
    public long remove(String key, Object... values){
        try {
            return redisTemplate.opsForSet().remove(key, values);
        }catch (Exception e){
            log.error("redis remove error:{}", e);
            return 0;
        }
    }

    /**
     * 获取list缓存的内容
     * @param key
     * @param start 开始
     * @param end 结束 0 到 -1代表所有制
     * @Return: java.util.List<java.lang.Object>
     * @Auth: huangyang
     * @Date: 2019/8/15 20:37
     */
    public List<Object> range(String key, long start, long end){
        try {
            return redisTemplate.opsForList().range(key, start, end);
        }catch (Exception e){
            log.error("redis range error:{}", e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     * @param key
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 20:39
     */
    public long sizeOfList(String key){
        try {
            return redisTemplate.opsForList().size(key);
        }catch (Exception e){
            log.error("redis size error:{}", e);
            return 0;
        }
    }

    /**
     * 通过索引获取list中的值
     * @param key
     * @param index
     * @Return: java.lang.Object
     * @Auth: huangyang
     * @Date: 2019/8/15 20:41
     */
    public Object index(String key, long index){
        try {
            return redisTemplate.opsForList().index(key, index);
        }catch (Exception e){
            log.error("redis index error:{}", e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     * @param key
     * @param value
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:49
     */
    public boolean rightPush(String key, Object value){
        try {
            redisTemplate.opsForList().rightPush(key,value);
            return true;
        }catch (Exception e){
            log.error("redis rightPush error:{}", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     * @param key
     * @param value
     * @param time 有效时间 单位(s)
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:50
     */
    public boolean rightPush(String key, Object value, long time){
        try {
            redisTemplate.opsForList().rightPush(key,value);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            log.error("redis rightPush error:{}", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     * @param key
     * @param value
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:52
     */
    public boolean rightPushAll(String key, List<Object> value){
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        }catch (Exception e){
            log.error("redis rightPushAll error:{}", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     * @param key
     * @param value
     * @param time 有效期 单位(s)
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:53
     */
    public boolean rightPushAll(String key, List<Object> value, long time){
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            log.error("redis rightPushAll error:{}", e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的数据
     * @param key
     * @param index
     * @param value
     * @Return: boolean
     * @Auth: huangyang
     * @Date: 2019/8/15 20:55
     */
    public boolean set(String key, long index, Object value){
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        }catch (Exception e){
            log.error("redis set error:{}", e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     * @param key
     * @param count
     * @param value
     * @Return: long
     * @Auth: huangyang
     * @Date: 2019/8/15 20:56
     */
    public long remove(String key, long count, Object value){
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        }catch (Exception e){
            log.error("redis remove error:{}", e);
            return 0;
        }
    }


}
