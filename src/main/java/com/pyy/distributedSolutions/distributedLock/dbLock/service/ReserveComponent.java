package com.pyy.distributedSolutions.distributedLock.dbLock.service;

import com.pyy.distributedSolutions.distributedLock.dbLock.dao.ReserveDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *  * program: distributed_solutions
 *  * Description:
 *  * User: BadYoung
 *  * Date:2022-06-25 22
 *  * Time:37
 *  
 */
@Component
public class ReserveComponent {
    @Resource
    private ReserveDAO reserveDAO;

    /**
     * 减库存
     */
    public void reduceInventory() {
        Integer reserve = reserveDAO.getReserve();
        if (reserve>0){
            reserveDAO.reduceInventory();
        }
    }
}
