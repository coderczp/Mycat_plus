#create database proxy_schema;
#use proxy_schema;

CREATE TABLE `cluster_node_info` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT 'proxy名称',
  `host` varchar(255) NOT NULL DEFAULT '' COMMENT '主机信息,必须是唯一的',
  `region` smallint(6) NOT NULL DEFAULT '0' COMMENT '分区(数据中心)编号',
  `startTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '启动时间',
  `port` smallint(6) NOT NULL DEFAULT '0' COMMENT '端口号',
  `weight` smallint(6) NOT NULL DEFAULT '0' COMMENT '进程权重',
  `webPort` varchar(255) NOT NULL DEFAULT '' COMMENT 'web管理端口',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `host_key` (`host`,`region`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='代理自身信息,只要用于集群,雪花片算法生成UUID时的机器ID等';


CREATE TABLE `logic_db` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `checkSQLschema` bit(1) NOT NULL,
  `sqlMaxLimit` smallint(6) NOT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `version` varchar(255) NOT NULL DEFAULT 'default' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE `logic_host` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `maxConn` int(11) DEFAULT NULL,
  `minConn` int(11) DEFAULT NULL,
  `heartbeatSql` varchar(255) DEFAULT 'select 1=1',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `version` varchar(255) DEFAULT 'default' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE `logic_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `shardingNodes` varchar(100) NOT NULL DEFAULT '',
  `shardingRule` varchar(100) NOT NULL DEFAULT '',
  `shardingColumn` varchar(100) NOT NULL DEFAULT '',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `logicDb` varchar(255) NOT NULL DEFAULT '',
  `sql` longtext NOT NULL,
  `primaryKey` varchar(255) NOT NULL DEFAULT '' COMMENT '表的主键',
  `autoIncrement` bit(1) DEFAULT NULL COMMENT '主键是否自动增长',
  `tableType` smallint(6) NOT NULL DEFAULT '0' COMMENT '表类型:0普通表 1全局表',
  `version` varchar(255) DEFAULT 'default' COMMENT '版本号',
  `ruleObjJson` varchar(255) NOT NULL DEFAULT '' COMMENT '分片规则对象的json',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`logicDb`),
  KEY `ldb_fk` (`logicDb`),
  CONSTRAINT `ldb_fk` FOREIGN KEY (`logicDb`) REFERENCES `logic_db` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE `logic_user` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(255) NOT NULL DEFAULT '' COMMENT '密码',
  `decrypt` varchar(255) DEFAULT NULL COMMENT '解密参数',
  `schemas` varchar(255) DEFAULT '' COMMENT '可以操作的数据',
  `type` smallint(6) NOT NULL DEFAULT '0' COMMENT '类型: 1只读',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `name_key` (`name`) COMMENT '唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='逻辑用户信息表';


CREATE TABLE `physics_host` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(11) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `host` varchar(100) NOT NULL,
  `port` smallint(6) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `user` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `version` varchar(255) DEFAULT 'default' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `n_key` (`name`),
  UNIQUE KEY `h_key` (`host`,`port`) COMMENT '主机端口索引'
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE `physics_logic_host_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `physicsHostId` int(11) DEFAULT NULL,
  `logicHostId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `h_key` (`physicsHostId`,`logicHostId`),
  CONSTRAINT `phid` FOREIGN KEY (`physicsHostId`) REFERENCES `physics_host` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE `sharding_node` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `physicsdb` varchar(100) NOT NULL,
  `logicHost` varchar(100) NOT NULL DEFAULT '',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `version` varchar(255) DEFAULT 'default' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `n_key` (`name`),
  KEY `lh_fk` (`logicHost`),
  CONSTRAINT `lh_fk` FOREIGN KEY (`logicHost`) REFERENCES `logic_host` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

INSERT INTO `physics_host` VALUES (1,NULL,'mysql','127.0.0.1',3306,'master','root','root','2018-04-16 06:18:22','default');
INSERT INTO `logic_host` VALUES (1,'vHost',100,10,'select 1=1','2018-04-16 06:22:28','default');
INSERT INTO `physics_logic_host_relation` VALUES (1,1,1);
INSERT INTO `logic_db` (`name`,`checkSQLschema`,`sqlMaxLimit`) VALUES('testdb',1,100);
INSERT INTO `sharding_node` VALUES (4,'dn','db0','vHost','2018-04-16 06:22:49','default'); 
INSERT INTO `logic_table` (`name`,`shardingNodes`,`shardingRule`,`shardingColumn`,`logicDb`,`primaryKey`,`autoIncrement`,`tableType`,`sql`,`ruleObjJson`) VALUES ('tpl','dn','ColumnValueMod','id','testdb','id',1,0,'CREATE TABLE `tpl`(\r\n`id` bigint(20) NOT NULL AUTO_INCREMENT,\r\n`name` varchar(64) DEFAULT NULL,\r\n  PRIMARY KEY (`id`)\r\n) ENGINE=InnoDB  DEFAULT CHARSET=utf8;','{\"name\":\"ColumnValueMod\",\"partitionNum\":1,\"class\":\"io.mycat.route.function.PartitionByMod\"}');
INSERT INTO `logic_user` (`Id`,`name`,`password`,`decrypt`,`schemas`,`type`) VALUES (1,'root','root',NULL,'all',0),(2,'read','read',NULL,'all',1);