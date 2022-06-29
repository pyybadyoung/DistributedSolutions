package com.pyy.distributedSolutions.distributedLock.BloomFilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 *  * program: distributed_solutions
 *  * Description:
 *  * User: BadYoung
 *  * Date:2022-06-29 06
 *  * Time:29
 *  google基于内存JVM内存实现的布隆过滤器
 */
public class GuavaBloomFilter {

    /** 预计插入的数据 */
    private static Integer expectedInsertions = 10000000;
    /** 误判率 */
    private static Double fpp = 0.01;
    /**
     * 布隆过滤器
     *  Funnels：数据类型，有Funels类指定即可
     *  long expectedInsertions：预期插入的值的数量
     *  fpp：错误率
     *  BloomFilter.Strategy：hash算法
     */
    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), expectedInsertions, fpp);

    public static void main(String[] args) {

        //插入一千万条数据
        for (int i = 0; i < expectedInsertions; i++) {
            bloomFilter.put(i);
        }

        // 用1千万数据测试误判率
        int count = 0;
        for (int i = expectedInsertions; i < expectedInsertions *2; i++) {
            if (bloomFilter.mightContain(i)) {
                count++;
            }
        }
        System.out.println("一共误判了：" + count);
    }

}
