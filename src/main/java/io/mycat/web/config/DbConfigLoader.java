/*
 * Copyright (c) 2018, MyCat_Plus and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */

package io.mycat.web.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import io.mycat.MycatServer;
import io.mycat.backend.datasource.PhysicalDBPool;
import io.mycat.config.loader.ConfigLoader;
import io.mycat.config.model.ClusterConfig;
import io.mycat.config.model.DBHostConfig;
import io.mycat.config.model.DataHostConfig;
import io.mycat.config.model.DataNodeConfig;
import io.mycat.config.model.FirewallConfig;
import io.mycat.config.model.MycatNodeConfig;
import io.mycat.config.model.SchemaConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.config.model.TableConfig;
import io.mycat.config.model.TableConfigMap;
import io.mycat.config.model.UserConfig;
import io.mycat.config.model.rule.RuleConfig;
import io.mycat.manager.response.ReloadConfig;
import io.mycat.route.function.AbstractPartitionAlgorithm;
import io.mycat.util.DecryptUtil;
import io.mycat.util.SplitUtil;
import io.mycat.web.bean.LogicDB;
import io.mycat.web.bean.LogicHost;
import io.mycat.web.bean.LogicTable;
import io.mycat.web.bean.LogicUser;
import io.mycat.web.bean.PhysicsHost;
import io.mycat.web.bean.ShardingNode;
import io.mycat.web.clsuter.ClusterManager;
import io.mycat.web.controller.ClusterController;
import io.mycat.web.dao.LogicDBDao;
import io.mycat.web.dao.LogicTableDao;
import io.mycat.web.dao.LogicUserDao;
import io.mycat.web.dao.PhysicsHostDao;
import io.mycat.web.dao.ShardingNodeDao;
import io.mycat.web.event.Event;
import io.mycat.web.event.Event.Type;
import io.mycat.web.event.EventHandler;
import io.mycat.web.event.EventManager;
import io.mycat.web.service.LogicHostService;
import io.mycat.web.util.HttpUtils;

/**
 * 从DB加载配置信息
 * @author [jeff.cao-coder_czp@126.com]
 * @version 0.0.1, 2018年4月10日 上午10:10:35 
 */
@Component
public class DbConfigLoader implements ConfigLoader, EventHandler {

    @Autowired
    private LogicHostService                       lDao;

    @Autowired
    private LogicTableDao                          ltDao;

    @Autowired
    private PhysicsHostDao                         pDao;

    @Autowired
    private ShardingNodeDao                        shDao;

    @Autowired
    private LogicDBDao                             dbDao;

    @Autowired
    private LogicUserDao                           userDao;

    @Autowired
    private ClusterManager                         clusterManager;

    /**以下数据在配置变更下会被动态刷新*/
    private volatile ClusterConfig                 cluster;
    private volatile SystemConfig                  system;
    private volatile FirewallConfig                firewall;
    private volatile Map<String, UserConfig>       users;
    private volatile Map<String, SchemaConfig>     schemas;
    private volatile Map<String, DataHostConfig>   dataHosts;
    private volatile Map<String, DataNodeConfig>   dataNodes;
    private volatile Map<String, String>           dataNodeDbTypeMap;
    private static Logger                          log              = LoggerFactory.getLogger(DbConfigLoader.class);

    //这里包含了所有版本的数据库配置,维度直到数据库和表,前端的session可以切换版本

    @Override
    public SchemaConfig getSchemaConfig(String schema) {
        return schemas.get(schema);
    }

    @Override
    public Map<String, DataNodeConfig> getDataNodes() {
        return dataNodes;
    }

    @Override
    public Map<String, SchemaConfig> getSchemaConfigs() {
        return schemas;
    }

    @Override
    public Map<String, DataHostConfig> getDataHosts() {
        return dataHosts;
    }

    @Override
    public SystemConfig getSystemConfig() {
        return system;
    }

    @Override
    public UserConfig getUserConfig(String user) {
        return users.get(user);
    }

    @Override
    public Map<String, UserConfig> getUserConfigs() {
        return users;
    }

    @Override
    public FirewallConfig getFirewallConfig() {
        return firewall;
    }

    @Override
    public ClusterConfig getClusterConfig() {
        return cluster;
    }

    private SystemConfig loadSystemConfig() {
        return clusterManager.getSystemConfig();
    }

    private Map<String, UserConfig> loadUserConfig() {
        LogicUser param = new LogicUser();
        Set<String> allSchemas = schemas.keySet();
        List<LogicUser> list = userDao.list(param);
        Map<String, UserConfig> users = new HashMap<String, UserConfig>(list.size());
        for (LogicUser user : list) {
            String passwordDecrypt = DecryptUtil.mycatDecrypt(user.getDecrypt(), user.getName(), user.getPassword());
            UserConfig tmp = new UserConfig();
            tmp.setName(user.getName());
            tmp.setDefaultAccount(true);
            tmp.setPassword(passwordDecrypt);
            tmp.setEncryptPassword(user.getDecrypt());
            /*
            String benchmark = null;
            if (null != benchmark) {
            tmp.setBenchmark(Integer.parseInt(benchmark));
            }
            */
            tmp.setReadOnly(LogicUser.READ_OLNY.equals(user.getType()));
            String sc = user.getSchemas();
            if ("all".equals(sc)) {
                tmp.setSchemas(allSchemas);
            } else {
                tmp.setSchemas(new HashSet<String>(Arrays.asList(sc.split(","))));
            }
            //加载用户 DML 权限
            loadPrivileges(tmp);
            users.put(tmp.getName(), tmp);
        }
        return users;
    }

    private void loadPrivileges(UserConfig user) {
        //不设置默认全部
        //        UserPrivilegesConfig privilegesConfig = new UserPrivilegesConfig();
        //        privilegesConfig.setCheck(false);
        //        UserPrivilegesConfig.SchemaPrivilege schemaPrivilege = new UserPrivilegesConfig.SchemaPrivilege();
        //        UserPrivilegesConfig.TablePrivilege tablePrivilege = new UserPrivilegesConfig.TablePrivilege();
        //        schemaPrivilege.setName("testdb");
        //        schemaPrivilege.setDml(dml1Array);
        //        tablePrivilege.setName(name2);
        //        tablePrivilege.setDml(dml2Array);
        //        schemaPrivilege.addTablePrivilege(name2, tablePrivilege);
        //        user.setPrivilegesConfig(privilegesConfig);

    }

    private ClusterConfig loadClusterConfig() {
        MycatNodeConfig info = clusterManager.getCurrentNode();
        ClusterConfig cluster = new ClusterConfig();
        cluster.getNodes().put(info.getName(), info);
        cluster.setCurrentNode(info);
        return cluster;
    }

    private FirewallConfig loadFirewall() {
        return new FirewallConfig();
    }


    /**
     * 加载所有版本的数据库配置
     * @return
     */
    private Map<String, Map<String, SchemaConfig>> loadAllVersioSchemas() {

        Map<String, Map<String, SchemaConfig>> all = new ConcurrentHashMap<>();

        LogicDB param = new LogicDB();
        List<LogicDB> list = dbDao.list(param);
        for (LogicDB db : list) {

            String version = db.getVersion();
            Map<String, SchemaConfig> schemas = all.get(version);
            if (schemas == null) {
                schemas = new HashMap<String, SchemaConfig>();
                all.put(version, schemas);
            }

            String name = db.getName();
            String dataNode = db.getDataNode();
            Integer sqlMaxLimit = db.getSqlMaxLimit();
            boolean checkSQLschema = db.isCheckSQLschema();
            Map<String, TableConfig> tables = loadTables(name, version);
            SchemaConfig cfg = new SchemaConfig(name, dataNode, tables, sqlMaxLimit, checkSQLschema);
            cfg.setDataNodeDbTypeMap(dataNodeDbTypeMap);
            schemas.put(name, cfg);
        }
        MultiVersionManager.getInstance().init(all);
        return all;
    }

    private Map<String, TableConfig> loadTables(String db, String version) {

        LogicTable param = new LogicTable();
        param.setVersion(version);
        param.setLogicDb(db);
        List<LogicTable> list = ltDao.list(param);

        Map<String, TableConfig> tables = new TableConfigMap();
        for (LogicTable tbl : list) {
            String subTables = null;
            boolean needAddLimit = true;
            boolean ruleRequired = false;
            int tableType = tbl.getTableType();
            String dataNode = tbl.getShardingNodes();
            String tname = tbl.getName().toUpperCase();
            boolean autoIncrement = tbl.isAutoIncrement();
            String primaryKey = tbl.getPrimaryKey().toUpperCase();

            //支持dataNode$0-n语法
            String[] dataNodeArr = SplitUtil.split(dataNode, ',', '$', '-');
            RuleConfig rule = buildShardingRule(tbl, dataNodeArr.length);
            Set<String> dbType = getDbType(dataNodeArr);

            TableConfig table = new TableConfig(tname, primaryKey, autoIncrement, needAddLimit, tableType, dataNode,
                dbType, rule, ruleRequired, null, false, null, null, subTables);
            tables.put(tname, table);
        }
        return tables;
    }

    private RuleConfig buildShardingRule(LogicTable tbl, int nodeCount) {
        try {
            String ruleObjJson = tbl.getRuleObjJson();
            JSONObject json = JSONObject.parseObject(ruleObjJson);
            String clsName = json.getString("class");
            Class<?> cls = Class.forName(clsName);
            AbstractPartitionAlgorithm rule = (AbstractPartitionAlgorithm) json.toJavaObject(cls);

            String shardingColumn = tbl.getShardingColumn().toUpperCase();
            RuleConfig cfg = new RuleConfig(shardingColumn, tbl.getShardingRule());
            cfg.setRuleAlgorithm(rule);

            return cfg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getDbType(String[] dataNodeArr) {
        Set<String> dbTypes = new HashSet<>();
        for (String node : dataNodeArr) {
            DataNodeConfig datanode = dataNodes.get(node);
            DataHostConfig datahost = dataHosts.get(datanode.getDataHost());
            dbTypes.add(datahost.getDbType());
        }
        return dbTypes;
    }

    private Map<String, DataNodeConfig> loadDataNodes() {

        Map<String, DataNodeConfig> dataNodes = new HashMap<>();
        Map<String, String> dataNodeDbTypeMap = new HashMap<>();

        ShardingNode param = new ShardingNode();
        List<ShardingNode> list = shDao.list(param);
        for (ShardingNode node : list) {
            String name = node.getName();
            String host = node.getLogicHost();

            DataHostConfig dh = dataHosts.get(host);
            dh.addDataNode(name);
            dataNodeDbTypeMap.put(name, dh.getDbType());

            dataNodes.put(name, new DataNodeConfig(name, node.getPhysicsdb(), host));
        }
        this.dataNodeDbTypeMap = dataNodeDbTypeMap;
        return dataNodes;
    }

    @PostConstruct
    public void onStart() throws Exception {
        loadConfig();
        EventManager.getInstance().registHandler(this);
    }

    //加载配置,顺序不能调整
    private void loadConfig() {
        MultiVersionManager instance = MultiVersionManager.getInstance();
        long start = System.currentTimeMillis();
        this.system = loadSystemConfig();
        this.cluster = loadClusterConfig();
        this.dataHosts = loadDataHosts();
        this.dataNodes = loadDataNodes();
        this.firewall = loadFirewall();
        this.loadAllVersioSchemas();
        this.schemas = instance.getConfig("default");
        this.users = loadUserConfig();
        long end = System.currentTimeMillis();
        log.info("success load config,time:{}", end - start);
    }

    private Map<String, DataHostConfig> loadDataHosts() {
        String dbType = "mysql";
        String dbDriver = "native";
        List<LogicHost> all = lDao.listAll();
        Map<String, DataHostConfig> dataHosts = new HashMap<>();

        for (LogicHost host : all) {
            DBHostConfig[] writeDbConfs = buildWriteHost(host.getPhysicsdbs(), host);
            Map<Integer, DBHostConfig[]> readHostsMap = buildReadHost(host);
            /**
             * 读取切换类型
             * -1 表示不自动切换
             * 1 默认值，自动切换
             * 2 基于MySQL主从同步的状态决定是否切换
             * 心跳询句为 show slave status
             * 3 基于 MySQL galary cluster 的切换机制
             */
            int switchType = -1;
            //读取从延迟界限
            int slaveThreshold = -1;
            boolean tempReadHostAvailable = true;
            DataHostConfig conf = new DataHostConfig(host.getName(), dbType, dbDriver, writeDbConfs, readHostsMap,
                switchType, slaveThreshold, tempReadHostAvailable);

            conf.setMaxCon(host.getMaxConn());
            conf.setMinCon(host.getMinConn());
            conf.setBalance(PhysicalDBPool.BALANCE_NONE);
            conf.setWriteType(PhysicalDBPool.WRITE_ONLYONE_NODE);
            conf.setHearbeatSQL(host.getHeartbeatSql());
            conf.setConnectionInitSql(host.getConnectionInitSql());
            //hostConf.setFilters(filters);
            // hostConf.setLogTime(logTime);
            //hostConf.setSlaveIDs(slaveIDs);
            dataHosts.put(conf.getName(), conf);
        }
        return dataHosts;
    }

    /**
     * 创建读机
     * @param host
     * @return
     */
    private Map<Integer, DBHostConfig[]> buildReadHost(LogicHost host) {
        Map<Integer, DBHostConfig[]> map = new HashMap<>();
        List<PhysicsHost> physicsdbs = host.getPhysicsdbs();
        int size = physicsdbs.size();
        for (int i = 0; i < size; i++) {
            PhysicsHost ph = physicsdbs.get(i);
            map.put(i, buildWriteHost(pDao.querySlavesByMasterId(ph.getId()), host));
        }
        return map;
    }

    /**
     * 创建写机
     * @param host 
     * @param host
     * @return
     */
    private DBHostConfig[] buildWriteHost(List<PhysicsHost> physicsdbs, LogicHost host) {
        String nodeUrl = null;
        String encryptPassword = null;
        int size = physicsdbs.size();
        DBHostConfig[] wb = new DBHostConfig[size];
        for (int i = 0; i < size; i++) {
            PhysicsHost ph = physicsdbs.get(i);
            DBHostConfig conf = new DBHostConfig(ph.getHost(), ph.getHost(), ph.getPort(), nodeUrl, ph.getUser(),
                ph.getPassword(), encryptPassword);
            conf.setDbType(ph.getType());
            conf.setMaxCon(host.getMaxConn());
            conf.setMinCon(host.getMinConn());
            conf.setFilters(ph.getFilters());
            //conf.setLogTime(ph.get);
            //conf.setWeight(weight);
            wb[i] = conf;
        }
        return wb;
    }

    @Override
    public Type type() {
        return Type.CONFIG_RELOAD;
    }

    @Override
    public void onEvent(Event event) {
        try {
            doReload();
            notifyOtherNode();
        } catch (Exception e) {
            log.error("reload config error", e);
        }
    }

    public void doReload() throws Exception {
        onStart();
        MycatServer.getInstance().getConfig().getConfigIniter().reload(false);
        ReloadConfig.reload();
    }

    private void notifyOtherNode() {
        MycatNodeConfig curNode = clusterManager.getCurrentNode();
        List<MycatNodeConfig> allNodes = clusterManager.getAllNodes();
        for (MycatNodeConfig node : allNodes) {
            if (curNode.getHost().equals(node.getHost()) && curNode.getPort() == node.getPort()) {
                continue;
            }
            HttpUtils.nodeRpc(node, ClusterController.UPDATE_API);
            log.info("success to notify:{}", node);
        }
    }
}
