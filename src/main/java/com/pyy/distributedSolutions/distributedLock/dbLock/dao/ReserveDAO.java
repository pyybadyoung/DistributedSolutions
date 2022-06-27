package com.pyy.distributedSolutions.distributedLock.dbLock.dao;

/**
 *  * program: distributed_solutions
 *  * Description:
 *  * User: BadYoung
 *  * Date:2022-06-25 22
 *  * Time:36
 *  
 */
public interface ReserveDAO {
    Integer reduceInventory();

    Integer getReserve();
}
