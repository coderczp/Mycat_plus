# Mycat_plus
Mycat 1.6深度优化版本,只支持mysql协议,专注于mysql代理,第一个大版本实现以下特性

0. 删除对log4j的直接依赖,全部改为slf4j
1. 删除非Mysql的支持
2. 支持web界面查看和管理配置
3. 内建常用的分片算法,支持通过Mysql的分区表语法自动创建分片.
4. 创建Mycat_information库,Mycat相关信息全部入库
5. 支持主流客户端(Mysql-front等)

第一版本预计7月发布

Mycat_plus官方QQ群: 344764947 欢迎加入,别忘记star