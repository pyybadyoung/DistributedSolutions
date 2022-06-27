CREATE TABLE `globallocktable` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lockKey` varchar(60) NOT NULL COMMENT '锁名称',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `lockKey` (`lockKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='全局锁';



<insert id="insertSelectiveWithTest" useGeneratedKeys="true" keyProperty="id" parameterType="com.javashitang.middleware.lock.mysql.pojo.GlobalLockTable">
  insert into `globallocktable` (`id`,
  `lockKey`, `createTime` )
   select #{id,jdbcType=INTEGER}, #{lockKey,jdbcType=VARCHAR}, #{createTime,jdbcType=TIMESTAMP}
   from dual where not exists
    (select 1 from globallocktable where lockKey = #{lockKey,jdbcType=VARCHAR})
</insert>