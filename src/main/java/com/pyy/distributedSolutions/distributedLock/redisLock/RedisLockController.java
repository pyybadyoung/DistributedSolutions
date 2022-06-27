package com.pyy.distributedSolutions.distributedLock.redisLock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *  * program: distributed_solutions
 *  * Description:
 *  * User: BadYoung
 *  * Date:2022-06-26 14
 *  * Time:23
 *  
 */
@RestController
@RequestMapping("/redisLock")
@Slf4j
public class RedisLockController {

    @Resource
    RedisTemplate<String,String> redisTemplate;

    private static final String MAOTAI = "maotai20210321001";//茅台商品编号

    private static final String RESOURCE_UNIQUE_IDENTIFIER = "423e08ec-a38e-4ff9-b928-1d2f98ee01b7";

    /**
     * 为什么要分布式锁的存在
     * 在集群或者分布式环境下，存在多个jvm进程
     * @return
     */
    @GetMapping("/seckillMaotai")
    public String seckillMaotai(){
        //synchronized锁制作用在本JVM中，如果出现在分布式或者集群环境中将会失去作用
        synchronized (this){
            Integer count = Integer.parseInt(redisTemplate.opsForValue().get(MAOTAI)); // 1
            if (count>0){
                //抢到了茅台，库存减一
                redisTemplate.opsForValue().set(MAOTAI,String.valueOf(count-1));
                //后续操作 do something
                log.info("我抢到茅台了!");
                return "ok";
            }else {
                return "no";
            }
        }
    }


    /**
     * redis实现分布式锁的第一个版本
     * 缺陷：如果锁超时了业务没有执行完就会有问题
     *       自己的锁也有可能被其他线程释放掉
     * @return
     */
    @GetMapping("/seckillMaotaiV1")
    public String seckillMaotaiV1(){
        //获取锁                                     如果不存在就设置一个值
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(RESOURCE_UNIQUE_IDENTIFIER, "1");
        //如果获取到锁了
        if (isLock){
            //设置锁的过期时间  设置过期时间的目的是：在获取到锁以后程序出现异常，没有把锁释放掉，导致其他线程一致获取不到锁
            redisTemplate.expire(MAOTAI,5, TimeUnit.SECONDS);
            try {
                //得到茅台的剩余量
                Integer count = Integer.parseInt(redisTemplate.opsForValue().get(RESOURCE_UNIQUE_IDENTIFIER)); // 1
                if (count>0){
                    //抢到了茅塔，库存减一
                    redisTemplate.opsForValue().set(MAOTAI,String.valueOf(count-1));
                    //后续操作 do something
                    log.info("我抢到茅台了!");
                    return "ok";
                }else {
                    return "no";
                }
            }finally {
                //释放锁  这里没有做到自己的锁自己释放
                redisTemplate.delete(RESOURCE_UNIQUE_IDENTIFIER);
            }
        }
        return "dont get lock";
    }


    /**
     * redis实现分布式锁的第二个版本
     * 缺陷：这个版本的分布式锁，虽然做到了自己的锁自己释放，但是锁超时业务没有执行完的情况还没有处理
     * @return
     */
    @GetMapping("/seckillMaotaiV2")
    public String seckillMaotaiV2(){
        //每个获取说的请求都有自己一个唯一的省份标识（相当于人的身份证）
        String requestid = UUID.randomUUID().toString() + Thread.currentThread().getId();
        //获取锁                                     //如果不存在 就设置值，带有身份的唯一标识，并设置过期时间
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(RESOURCE_UNIQUE_IDENTIFIER, requestid, 5,TimeUnit.SECONDS);
        //如果获取到锁了
        if (isLock){
            try {
                //得到茅台的剩余量
                Integer count = Integer.parseInt(redisTemplate.opsForValue().get(RESOURCE_UNIQUE_IDENTIFIER)); // 1
                if (count>0){
                    //抢到了茅塔，库存减一
                    redisTemplate.opsForValue().set(MAOTAI,String.valueOf(count-1));
                    //后续操作 do something
                    log.info("我抢到茅台了!");
                    return "ok";
                }else {
                    return "no";
                }
            }finally {
                String id = redisTemplate.opsForValue().get(RESOURCE_UNIQUE_IDENTIFIER);
                //这里加锁的是自己才能把锁释放掉
                if (id!=null&&id.equals(requestid)){
                    //释放锁
                    redisTemplate.delete(RESOURCE_UNIQUE_IDENTIFIER);
                }
            }
        }
        return "dont get lock";
    }


    //模拟一下守护线程为其续期
    ScheduledExecutorService executorService;//创建守护线程池
    ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();//队列

    @PostConstruct
    public void init(){
        //初始化续命线程池数量    newScheduledThreadPool 线程池的原理
        executorService = Executors.newScheduledThreadPool(1);

        //编写续期的lua
        String expirrenew = "" +
                "if redis.call('get',KEYS[1]) == ARGV[1] then redis.call('expire',KEYS[1],ARGV[2]) ; return true " +
                "else return false " +
                "end";

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Iterator<String> iterator = set.iterator();
                while (iterator.hasNext()){
                    String rquestid = iterator.next();
                    redisTemplate.execute(new RedisCallback<Boolean>() {
                        @Override
                        public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                            Boolean eval = false;
                            try {
                                eval = redisConnection.eval(
                                        expirrenew.getBytes(),
                                        ReturnType.BOOLEAN,
                                        1,
                                        RESOURCE_UNIQUE_IDENTIFIER.getBytes(),
                                        rquestid.getBytes(),
                                        "5".getBytes()
                                );
                            } catch (Exception e) {
                                log.error("锁续期失败,{}",e.getMessage());
                            }
                            return eval;
                        }
                    });
                }

            }
        },0,1,TimeUnit.SECONDS);
    }

    /**
     * redis实现分布式锁的第三个版本
     *      解决锁超时，任务没有执行完的问题
     * 疑问：目前的这个锁还有哪些缺陷，，是否支持重入，，是否是阻塞的
     * @return
     */
    @GetMapping("/seckillMaotaiV3")
    public String seckillMaotaiV3(){
        //每个获取说的请求都有自己一个唯一的省份标识（相当于人的身份证）
        String requestid = UUID.randomUUID().toString() + Thread.currentThread().getId();
        //获取锁                                     //如果不存在 就设置值，带有身份的唯一标识，并设置过期时间
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(RESOURCE_UNIQUE_IDENTIFIER, requestid, 5,TimeUnit.SECONDS);
        //如果获取到锁了
        if (isLock){
            //获取锁成功后让守护线程为其续命
            set.add(requestid);
            try {
                //得到茅台的剩余量
                Integer count = Integer.parseInt(redisTemplate.opsForValue().get(RESOURCE_UNIQUE_IDENTIFIER)); // 1
                if (count>0){
                    //抢到了茅塔，库存减一
                    redisTemplate.opsForValue().set(MAOTAI,String.valueOf(count-1));
                    //模拟业务超时
                    TimeUnit.SECONDS.sleep(10);
                    log.info("我抢到茅台了!");
                    return "ok";
                }else {
                    return "no";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                String id = redisTemplate.opsForValue().get(RESOURCE_UNIQUE_IDENTIFIER);
//                //这里加锁的是自己才能把锁释放掉
//                if (id!=null&&id.equals(requestid)){
//                    //释放锁
//                    redisTemplate.delete(RESOURCE_UNIQUE_IDENTIFIER);
//                }
                //解除锁续期
                set.remove(requestid);
            }
        }
        return "dont get lock";
    }



}
