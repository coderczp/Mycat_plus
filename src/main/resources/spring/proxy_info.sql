CREATE TABLE `mysql_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(11),
  `name` varchar(100) NOT NULL,
  `host` varchar(100) NOT NULL,
  `port` smallint(6) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `user` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `db_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `logicdb` varchar(100) NOT NULL,
  `physicsdb` varchar(100) NOT NULL,
  `mysqlInfoName` varchar(100) NOT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
