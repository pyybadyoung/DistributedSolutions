package com.pyy.distributedSolutions.distributedLock.dbLock.pojo;

import lombok.Data;

import java.util.Date;
@Data
public class GlobalLockTable {

  private Integer id;
  private String lockKey;
  private Date createTime;
  // 省略get和set方法
}