package com.pyy.distributedSolutions.distributedLock.BloomFilter.mutex;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 *  * program: distributed_solutions
 *  * Description:  互斥锁解决缓存击穿问题
 *  * User: BadYoung
 *  * Date:2022-06-29 22
 *  * Time:19
 *  
 */
@RestController
@RequestMapping("/mutex")
public class MutexController {

    @Resource
    RedisTemplate<String,String> redisTemplate;
    //互斥锁的key
    private static final String  MUTEX = "lock";

    @GetMapping("/testMutex")
    public String testMutex(String name){
        //获取缓存数据
        String mutex = redisTemplate.opsForValue().get(name);
        //如果能拿到数据就直接返回
        if (!StringUtils.isEmpty(mutex)){
            return mutex;
        }
        // 4.无,则获取互斥锁
        String lockKey = MUTEX + name;
        Boolean isLock = tryLock(lockKey);

        try {
            //没有获取到互斥锁
            if (!isLock){
                return "没有获取到互斥锁";
            }
            //获取到互斥锁
            //从数据库或者其他地方将数据查到
            //如果查到了就将真实的数据写到缓存中
            //如果没有查到就写一个空值到缓存中
            redisTemplate.opsForValue().set(name,null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 11.释放锁
            unLock(lockKey);

        }
        return null;
    }

    /**
     * 释放锁
     * @param lockKey
     */
    private void unLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    /**
     * 获取互斥锁
     * @param lockKey
     * @return
     */
    private Boolean tryLock(String lockKey) {
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        return aBoolean;
    }
}
