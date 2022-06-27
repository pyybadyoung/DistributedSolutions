package com.pyy.distributedSolutions.distributedLock.dbLock.controller;

import com.pyy.distributedSolutions.distributedLock.dbLock.service.GlobalLockComponent;
import com.pyy.distributedSolutions.distributedLock.dbLock.service.ReserveComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *  * program: distributed_solutions
 *  * Description:
 *  * User: BadYoung
 *  * Date:2022-06-24 21
 *  * Time:41
 *  
 */
@RestController
@RequestMapping("/dbLock")
public class DBLockController {

    @Resource
    private GlobalLockComponent globalLockComponent;
    @Resource
    private ReserveComponent reserveComponent;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/testDBLock")
    @Transactional
    public String testDBLock(String name){
        System.out.println("访问："+serverPort);
        if (!globalLockComponent.tryLock(name)) {
            // 没有获取到锁返回
            return "没有获取到锁返回";
        }
        try {
            // 这里写业务逻辑  进行扣减库存的操作
            reserveComponent.reduceInventory();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            globalLockComponent.releasLock(name);
        }
        return "获取到锁，任务执行结束";
    }


    @GetMapping("/tryLockWithClear")
    @Transactional
    public String tryLockWithClear(String name){

        if (!globalLockComponent.tryLockWithClear(name,10L)) {
            // 没有获取到锁返回
            return "没有获取到锁返回";
        }
        try {
            // 这里写业务逻辑  进行扣减库存的操作
            reserveComponent.reduceInventory();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            globalLockComponent.releasLock(name);
        }
        return "获取到锁，任务执行结束";
    }
}
