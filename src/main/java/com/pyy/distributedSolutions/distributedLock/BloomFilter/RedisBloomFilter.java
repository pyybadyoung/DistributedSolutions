package com.pyy.distributedSolutions.distributedLock.BloomFilter;

import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 *  * program: distributed_solutions
 *  * Description: redission布隆过滤器
 *  * User: BadYoung
 *  * Date:2022-06-29 21
 *  * Time:41
 *  
 */
public class RedisBloomFilter {

    /** 预计插入的数据 */
    private static Integer expectedInsertions = 10000;
    /** 误判率 */
    private static Double fpp = 0.01;

    public static void main(String[] args) {
        // Redis连接配置，无密码
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.119.128:6379");
        config.useSingleServer().setPassword("000415");

        RedissonClient redissonClient = Redisson.create(config);

        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("user");
        //redission是根据存入的数据量和误判率和计算得到hash 函数的个数
        bloomFilter.tryInit(expectedInsertions,fpp);

        // 布隆过滤器增加元素
        for (Integer i = 0; i < expectedInsertions; i++) {
            bloomFilter.add(i);
        }

        // 统计元素
        int count = 0;
        for (int i = expectedInsertions; i < expectedInsertions*2; i++) {
            if (bloomFilter.contains(i)) {
                count++;
            }
        }
        System.out.println("误判次数" + count);
    }
}
