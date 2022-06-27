package com.pyy.distributedSolutions.distributedLock.redissonLock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *  * program: distributed_solutions
 *  * Description: Redisson 框架实现分布式锁
 *  * User: BadYoung
 *  * Date:2022-06-26 22
 *  * Time:14
 *  
 */
@RestController
@RequestMapping("/redisLock")
@Slf4j
public class RedissonController {

    @Value("${spring.redis.host}")
    String host;
    @Value("${spring.redis.port}")
    String port;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    private static final String MAOTAI = "maotai20210321001";//茅台商品编号

    private static final String RESOURCE_UNIQUE_IDENTIFIER = "423e08ec-a38e-4ff9-b928-1d2f98ee01b7";


//    //单机
//    RedissonClient redisson = Redisson.create();
//    Config config = new Config();
//    config.useSingleServer().setAddress("myredisserver:6379");
//
//
//
//    //主从
//    config.useMasterSlaveServers().setMasterAddress("127.0.0.1:6379").addSlaveAddress("127.0.0.1:6389","127.0.0.1:6332","127.0.0.1:6419")
//    .addSlaveAddress("127.0.0.1:6399");
//
//    //哨兵
//    config.useSentinelServers().setMasterName("mymaster").addSentinelAddress("127.0.0.1:26389","127.0.0.1:26379").addSentinelAddress("127.0.0.1:26319");
//
//
//
//    //集群
//    config.useClusterServers().setScanInterval(2000)// cluster state scan interval in milliseconds
//    .addNodeAddress("127.0.0.1:7000","127.0.0.1:7001").addNodeAddress("127.0.0.1:7002");
//


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }

    //注入RedissonClient客户端
    @Autowired
    RedissonClient redissonClient;

    /**
     * 使用Redisson框架实现分布式锁进行减库存操作
     *
     * @return
     */
    @GetMapping("/seckillMaotaiV4")
    public String seckillMaotaiV4() {
        RLock lock = redissonClient.getLock(RESOURCE_UNIQUE_IDENTIFIER);
        lock.lock();
        try {
            Integer count = Integer.parseInt(redisTemplate.opsForValue().get(MAOTAI)); // 1
            //如果还有库存
            if (count > 0) {
                //抢到了茅台，库存减一
                redisTemplate.opsForValue().set(MAOTAI, String.valueOf(count - 1));
                //后续操作 do something
                log.info("我抢到茅台了!");
                return "ok";
            } else {
                return "no";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            ;
        }
        return "";
    }
}
