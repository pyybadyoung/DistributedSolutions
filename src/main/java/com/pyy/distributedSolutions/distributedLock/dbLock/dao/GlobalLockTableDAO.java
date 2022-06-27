package com.pyy.distributedSolutions.distributedLock.dbLock.dao;


import com.pyy.distributedSolutions.distributedLock.dbLock.pojo.GlobalLockTable;

public interface GlobalLockTableDAO {

  int deleteByPrimaryKey(Integer id);

  int deleteByLockKey(String lockKey);

  GlobalLockTable selectByLockKey(String key);

  int insertSelectiveWithTest(GlobalLockTable record);
}