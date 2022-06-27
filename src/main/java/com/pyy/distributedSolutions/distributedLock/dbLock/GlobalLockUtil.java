package com.pyy.distributedSolutions.distributedLock.dbLock;

import com.pyy.distributedSolutions.distributedLock.dbLock.dao.GlobalLockTableDAO;
import com.pyy.distributedSolutions.distributedLock.dbLock.pojo.GlobalLockTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class GlobalLockUtil {

  private static Logger logger = LoggerFactory.getLogger(GlobalLockUtil.class);

  private static GlobalLockTable tryLockInternal(GlobalLockTableDAO lockDAO, String key) {
    GlobalLockTable insert = new GlobalLockTable();
    insert.setCreateTime(new Date());
    insert.setLockKey(key);
    // 注意的地方1
    int count = lockDAO.insertSelectiveWithTest(insert);
    if (count == 0) {
      GlobalLockTable ready = lockDAO.selectByLockKey(key);
      logger.warn("can not lock the key: {}, {}, {}", insert.getLockKey(), ready.getCreateTime(),
          ready.getId());
      return ready;
    }
    logger.info("yes got the lock by key: {}", insert.getId(), insert.getLockKey());
    return null;
  }

  /** 超时清除锁占用，并重新加锁 **/
  public static boolean tryLockWithClear(GlobalLockTableDAO lockDAO, String key, Long timeoutMs) {
    GlobalLockTable lock = tryLockInternal(lockDAO, key);
    if (lock == null) return true;
    if (System.currentTimeMillis() - lock.getCreateTime().getTime() <= timeoutMs) {
      logger.warn("sorry, can not get the key. : {}, {}, {}", key, lock.getId(), lock.getCreateTime());
      return false;
    }
    logger.warn("the key already timeout wthin : {}, {}, will clear", key, timeoutMs);
    // 注意的地方2
    int count = lockDAO.deleteByPrimaryKey(lock.getId());
    if (count == 0) {
      logger.warn("sorry, the key already preemptived by others: {}, {}", lock.getId(), lock.getLockKey());
      return false;
    }
    lock = tryLockInternal(lockDAO, key);
    return lock != null ? false : true;
  }

  /** 加锁 **/
  public static boolean tryLock(GlobalLockTableDAO lockDAO, String key) {
    return tryLockInternal(lockDAO, key) == null ? true : false;
  }

  /** 解锁 **/
  public static void releasLock(GlobalLockTableDAO lockDAO, String key) {
    lockDAO.deleteByLockKey(key);
  }
}