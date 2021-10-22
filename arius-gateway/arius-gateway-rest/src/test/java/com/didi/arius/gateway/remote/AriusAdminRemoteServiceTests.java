package com.didi.arius.gateway.remote;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.remote.response.*;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class AriusAdminRemoteServiceTests {

    private static String CLUSTER = "dc-es02";


    @Autowired
    private AriusAdminRemoteService ariusAdminRemoteService;

    @Before
    public void setUp() {
    }

    @Test
    public void testListApp() {
        AppListResponse appListResponse = ariusAdminRemoteService.listApp();
        System.out.println(JSON.toJSONString(appListResponse));
        assertEquals(appListResponse.getData().size() >= 0, true);
        //{"code":0,"data":[{"aggrAnalyzeEnable":1,"analyzeResponseEnable":1,"cluster":"","dslAnalyzeEnable":1,"id":100000003,"indexExp":["cn_arius_stats_node_info*","dsink_error_metrics*","us01_arius_stats_node_info*","arius_stats_info*","swan_agent_metrics*","dsink_metrics*","arius_info*","latest_swan_agent_metrics*","cn_arius_stats_index_node_info*","cn_arius_stats_index_info*","kafka_metrics*"],"ip":[],"isRoot":0,"isSourceSeparated":0,"name":"fqftest","queryThreshold":100,"searchType":1,"verifyCode":"cVWLBbv6AS8rVju","windexExp":[]},{"aggrAnalyzeEnable":1,"analyzeResponseEnable":1,"cluster":"","dslAnalyzeEnable":1,"id":100000001,"indexExp":["cn_arius_stats_node_info*","dsink_error_metrics*","us01_arius_stats_node_info*","arius_stats_info*","swan_agent_metrics*","dsink_metrics*","arius_info*","latest_swan_agent_metrics*","cn_arius_stats_index_node_info*","cn_arius_stats_index_info*","kafka_metrics*"],"ip":[],"isRoot":0,"isSourceSeparated":0,"name":"33888","queryThreshold":100,"searchType":1,"verifyCode":"SNE8U2bkOFnSLNG","windexExp":[]},{"aggrAnalyzeEnable":1,"analyzeResponseEnable":1,"cluster":"{clusterName}","dslAnalyzeEnable":1,"id":100000000,"indexExp":["*"],"ip":[],"isRoot":1,"isSourceSeparated":0,"name":"admin","queryThreshold":100,"searchType":1,"verifyCode":"azAWiJhxkho33ac","windexExp":["*"]},{"aggrAnalyzeEnable":1,"analyzeResponseEnable":1,"cluster":"","dslAnalyzeEnable":1,"id":9,"indexExp":["*"],"ip":[],"isRoot":1,"isSourceSeparated":0,"name":"高版本ES查询回放","queryThreshold":100,"searchType":1,"verifyCode":"helloworld","windexExp":["*"]},{"aggrAnalyzeEnable":1,"analyzeResponseEnable":1,"cluster":"","dslAnalyzeEnable":1,"id":7,"indexExp":["*"],"ip":[],"isRoot":1,"isSourceSeparated":0,"name":"低版本ES查询回放","queryThreshold":100,"searchType":1,"verifyCode":"helloworld","windexExp":["*"]},{"aggrAnalyzeEnable":1,"analyzeResponseEnable":1,"cluster":"","dslAnalyzeEnable":1,"id":5,"indexExp":["test-dev-1","us01_arius_stats_node_info*","arius.master.slave.check","cn_arius.indexname.access","cn_arius_stats_ingest_info*","cn_arius.appid.template.access","cn_arius.template.access","cn_arius_stats_index_node_info*","cn_arius_stats_index_info*","kafka_metrics*","arius.dsl.template","arius.template.mapping","swan_agent_metrics*","cn_arius_stats_node_index_info*","dsink_metrics*","cn_arius_template_quota_usage","arius.index.size","latest_swan_agent_metrics*","cn_arius_stats_cluster_info*","arius.template.hit","arius.dsl.field.use","cn_arius_stats_node_info*","arius_stats_info*","arius_info*","cn_arius_meta_job_log*","cn_v2.arius.template.label","arius.template.field","arius.dsl.metrics*","arius.dsl.analyze.result","cn_record.arius.template.value*","dsink_error_metrics*","cn_arius_stats_dcdr_info*","cn_record_arius_template_quota_usage*","cn_arius_meta_server_log*","cn_arius.indexname.collect","arius.gateway.join*","cn_arius_template_qutoa_notiry_record*"],"ip":[],"isRoot":0,"isSourceSeparated":0,"name":"dsl审核","queryThreshold":100,"searchType":1,"verifyCode":"helloworld","windexExp":["test-dev-1","arius.master.slave.check","cn_arius.indexname.access","cn_arius_stats_ingest_info*","cn_arius.appid.template.access","cn_arius.template.access","cn_arius_stats_index_node_info*","cn_arius_stats_index_info*","arius.dsl.template","arius.template.mapping","cn_arius_stats_node_index_info*","cn_arius_template_quota_usage","arius.index.size","cn_arius_stats_cluster_info*","arius.template.hit","arius.dsl.field.use","cn_arius_stats_node_info*","cn_arius_meta_job_log*","arius.template.field","arius.dsl.metrics*","arius.dsl.analyze.result","cn_record.arius.template.value*","cn_arius_stats_dcdr_info*","cn_record_arius_template_quota_usage*","cn_arius_meta_server_log*","cn_arius.indexname.collect","arius.gateway.join*","cn_arius_template_qutoa_notiry_record*"]},{"aggrAnalyzeEnable":1,"analyzeResponseEnable":1,"cluster":"","dslAnalyzeEnable":1,"id":3,"indexExp":["*"],"ip":[],"isRoot":1,"isSourceSeparated":0,"name":"商业数据专用","queryThreshold":100,"searchType":1,"verifyCode":"ocDk1kKl37NJRCG","windexExp":["*"]},{"aggrAnalyzeEnable":1,"analyzeResponseEnable":0,"cluster":"","dslAnalyzeEnable":0,"id":1,"indexExp":["*"],"ip":["100.69.144.31","127.0.0.1","100.90.90.36","100.90.89.31","100.90.146.53","100.70.179.19","10.97.207.35"],"isRoot":1,"isSourceSeparated":0,"name":"admin","queryThreshold":100,"searchType":1,"verifyCode":"admin","windexExp":["*"]}],"message":"操作成功"}
    }

    @Test
    public void testListCluster() {
        DataCenterListResponse dataCenterListResponse = ariusAdminRemoteService.listCluster();
        System.out.println(JSON.toJSONString(dataCenterListResponse));
        assertEquals(dataCenterListResponse.getData().size() >= 0, true);
        //{"code":0,"data":[{"cluster":"dc-es02","desc":"日常部署日常部署2222","esVersion":"7.6.1.302","httpAddress":"10.168.56.135:8060,10.169.182.134:8060","httpWriteAddress":"10.169.182.134:8060","id":57,"password":"","readAddress":"","type":3,"writeAddress":""},{"cluster":"d1","desc":"asadsasdasdasdasdasd","esVersion":"7.6.0","httpAddress":"10.86.119.55:8060,10.86.119.56:8060,10.86.119.57:8060","httpWriteAddress":"10.86.119.55:8060,10.86.119.56:8060,10.86.119.57:8060","id":167,"password":"","readAddress":"","type":4,"writeAddress":""},{"cluster":"d3","desc":"adadasd","esVersion":"7.6.0","httpAddress":"10.86.119.55:8060,10.86.119.56:8060,10.86.119.57:8060","httpWriteAddress":"10.86.119.55:8060,10.86.119.56:8060,10.86.119.57:8060","id":169,"password":"","readAddress":"","type":4,"writeAddress":""}],"message":"操作成功"}
    }

    @Test
    public void testGetTemplateInfoMap() {
        final TemplateInfoListResponse templateInfoMap = ariusAdminRemoteService.getTemplateInfoMap(CLUSTER);
        System.out.println(JSON.toJSONString(templateInfoMap));
        assertEquals(templateInfoMap.getData().size() >= 0, true);
        //{"code":0,"data":{"test-dev-1":{"aliases":[],"expression":"test-dev-1","id":1089,"version":0},"arius.master.slave.check":{"aliases":[],"expression":"arius.master.slave.check","id":1043,"version":0},"cn_arius.indexname.access":{"aliases":[],"expression":"cn_arius.indexname.access","id":1053,"version":0},"cn_arius_stats_ingest_info*":{"aliases":[],"expression":"cn_arius_stats_ingest_info*","id":1071,"version":0},"cn_arius.appid.template.access":{"aliases":[],"expression":"cn_arius.appid.template.access","id":1051,"version":0},"cn_arius.template.access":{"aliases":[],"expression":"cn_arius.template.access","id":1057,"version":0},"cn_arius_stats_index_info*":{"aliases":[],"expression":"cn_arius_stats_index_info*","id":1067,"version":0},"arius.dsl.template":{"aliases":[{"name":"dsl_query_limit"}],"expression":"arius.dsl.template","id":1037,"version":0},"arius.template.mapping":{"aliases":[],"expression":"arius.template.mapping","id":1049,"version":0},"cn_arius_stats_node_index_info*":{"aliases":[],"expression":"cn_arius_stats_node_index_info*","id":1073,"version":0},"arius.index.size":{"aliases":[],"expression":"arius.index.size","id":1041,"version":0},"cn_arius_template_quota_usage":{"aliases":[],"expression":"cn_arius_template_quota_usage","id":1077,"version":0},"arius.template.hit":{"aliases":[],"expression":"arius.template.hit","id":1047,"version":0},"cn_arius_stats_cluster_info*":{"aliases":[],"expression":"cn_arius_stats_cluster_info*","id":1063,"version":0},"arius.dsl.field.use":{"aliases":[],"expression":"arius.dsl.field.use","id":1033,"version":0},"cn_arius_stats_node_info*":{"aliases":[],"expression":"cn_arius_stats_node_info*","id":1075,"version":0},"cn_arius_meta_job_log*":{"aliases":[],"expression":"cn_arius_meta_job_log*","id":1059,"version":0},"arius.dsl.metrics*":{"aliases":[],"expression":"arius.dsl.metrics*","id":1035,"version":0},"arius.template.field":{"aliases":[],"expression":"arius.template.field","id":1045,"version":0},"cn_v2.arius.template.label":{"aliases":[],"expression":"cn_v2.arius.template.label","id":1085,"version":0},"arius.dsl.analyze.result":{"aliases":[],"expression":"arius.dsl.analyze.result","id":1031,"version":0},"cn_record.arius.template.value*":{"aliases":[],"expression":"cn_record.arius.template.value*","id":1081,"version":0},"cn_arius_stats_dcdr_info*":{"aliases":[],"expression":"cn_arius_stats_dcdr_info*","id":1065,"version":0},"arius.gateway.join*":{"aliases":[],"expression":"arius.gateway.join*","id":1039,"version":0},"cn_arius.indexname.collect":{"aliases":[],"expression":"cn_arius.indexname.collect","id":1055,"version":0},"cn_arius_meta_server_log*":{"aliases":[],"expression":"cn_arius_meta_server_log*","id":1061,"version":0},"cn_record_arius_template_quota_usage*":{"aliases":[],"expression":"cn_record_arius_template_quota_usage*","id":1083,"version":0},"cn_arius_template_qutoa_notiry_record*":{"aliases":[],"expression":"cn_arius_template_qutoa_notiry_record*","id":1079,"version":0}},"message":"操作成功"}
    }

    @Test
    public void testGetAliveCount() {
        final ActiveCountResponse aliveCount = ariusAdminRemoteService.getAliveCount(CLUSTER);
        System.out.println(JSON.toJSONString(aliveCount));
        assertEquals(aliveCount.getData() >= 0, true);
        //{"code":0,"data":0,"message":"操作成功"}
    }

    @Test
    public void testListDeployInfo() {
        final IndexTemplateListResponse indexTemplateListResponse = ariusAdminRemoteService.listDeployInfo();
        System.out.println(JSON.toJSONString(indexTemplateListResponse));
        assertEquals(indexTemplateListResponse.getData().size() >= 0, true);
        //{"code":0,"data":{"test-dev-1":{"baseInfo":{"aliases":[],"dateField":"","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"test-dev-1","id":1091,"ingestPipeline":"test-dev-1","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"test-dev-1"},"slaveInfos":[]},"arius.master.slave.check":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.master.slave.check","id":1043,"ingestPipeline":"arius.master.slave.check","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.master.slave.check"},"slaveInfos":[]},"cn_arius.indexname.access":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"cn_arius.indexname.access","id":1053,"ingestPipeline":"cn_arius.indexname.access","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius.indexname.access"},"slaveInfos":[]},"cn_arius.appid.template.access":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"cn_arius.appid.template.access","id":1051,"ingestPipeline":"cn_arius.appid.template.access","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius.appid.template.access"},"slaveInfos":[]},"arius.dsl.metrics":{"baseInfo":{"aliases":[],"dateField":"timeStamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":150,"expression":"arius.dsl.metrics*","id":1035,"ingestPipeline":"arius.dsl.metrics","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.dsl.metrics"},"slaveInfos":[]},"cn_arius.template.access":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"cn_arius.template.access","id":1057,"ingestPipeline":"cn_arius.template.access","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius.template.access"},"slaveInfos":[]},"cn_arius_stats_node_index_info":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":7,"expression":"cn_arius_stats_node_index_info*","id":1073,"ingestPipeline":"cn_arius_stats_node_index_info","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_stats_node_index_info"},"slaveInfos":[]},"arius.dsl.template":{"baseInfo":{"aliases":["dsl_query_limit"],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.dsl.template","id":1037,"ingestPipeline":"arius.dsl.template","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.dsl.template"},"slaveInfos":[]},"arius.template.mapping":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.template.mapping","id":1049,"ingestPipeline":"arius.template.mapping","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.template.mapping"},"slaveInfos":[]},"cn_arius_stats_dcdr_info":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":14,"expression":"cn_arius_stats_dcdr_info*","id":1065,"ingestPipeline":"cn_arius_stats_dcdr_info","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_stats_dcdr_info"},"slaveInfos":[]},"arius.index.size":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.index.size","id":1041,"ingestPipeline":"arius.index.size","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.index.size"},"slaveInfos":[]},"cn_arius_template_quota_usage":{"baseInfo":{"aliases":[],"dateField":"time","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"cn_arius_template_quota_usage","id":1077,"ingestPipeline":"cn_arius_template_quota_usage","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_template_quota_usage"},"slaveInfos":[]},"cn_arius_meta_server_log":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":6,"expression":"cn_arius_meta_server_log*","id":1061,"ingestPipeline":"cn_arius_meta_server_log","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_meta_server_log"},"slaveInfos":[]},"cn_arius_stats_ingest_info":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":7,"expression":"cn_arius_stats_ingest_info*","id":1071,"ingestPipeline":"cn_arius_stats_ingest_info","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_stats_ingest_info"},"slaveInfos":[]},"arius.template.hit":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.template.hit","id":1047,"ingestPipeline":"arius.template.hit","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.template.hit"},"slaveInfos":[]},"arius.dsl.field.use":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.dsl.field.use","id":1033,"ingestPipeline":"arius.dsl.field.use","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.dsl.field.use"},"slaveInfos":[]},"cn_arius_template_qutoa_notiry_record":{"baseInfo":{"aliases":[],"dateField":"zeroDate","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":7,"expression":"cn_arius_template_qutoa_notiry_record*","id":1079,"ingestPipeline":"cn_arius_template_qutoa_notiry_record","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_template_qutoa_notiry_record"},"slaveInfos":[]},"cn_arius_meta_job_log":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":2,"expression":"cn_arius_meta_job_log*","id":1059,"ingestPipeline":"cn_arius_meta_job_log","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_meta_job_log"},"slaveInfos":[]},"arius.template.field":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.template.field","id":1045,"ingestPipeline":"arius.template.field","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.template.field"},"slaveInfos":[]},"cn_v2.arius.template.label":{"baseInfo":{"aliases":[],"dateField":"","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"cn_v2.arius.template.label","id":1085,"ingestPipeline":"cn_v2.arius.template.label","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_v2.arius.template.label"},"slaveInfos":[]},"arius.dsl.analyze.result":{"baseInfo":{"aliases":[],"dateField":"logTime","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"arius.dsl.analyze.result","id":1031,"ingestPipeline":"arius.dsl.analyze.result","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.dsl.analyze.result"},"slaveInfos":[]},"cn_arius_stats_node_info":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":180,"expression":"cn_arius_stats_node_info*","id":1075,"ingestPipeline":"cn_arius_stats_node_info","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_stats_node_info"},"slaveInfos":[]},"cn_record_arius_template_quota_usage":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM","deployStatus":3,"expireTime":360,"expression":"cn_record_arius_template_quota_usage*","id":1083,"ingestPipeline":"cn_record_arius_template_quota_usage","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_record_arius_template_quota_usage"},"slaveInfos":[]},"arius.gateway.join":{"baseInfo":{"aliases":[],"dateField":"timeStamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":23,"expression":"arius.gateway.join*","id":1039,"ingestPipeline":"arius.gateway.join","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"arius.gateway.join"},"slaveInfos":[]},"cn_arius_stats_cluster_info":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":180,"expression":"cn_arius_stats_cluster_info*","id":1063,"ingestPipeline":"cn_arius_stats_cluster_info","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_stats_cluster_info"},"slaveInfos":[]},"cn_arius.indexname.collect":{"baseInfo":{"aliases":[],"dateField":"","dateFormat":"","deployStatus":3,"expireTime":-1,"expression":"cn_arius.indexname.collect","id":1055,"ingestPipeline":"cn_arius.indexname.collect","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius.indexname.collect"},"slaveInfos":[]},"cn_arius_stats_index_info":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM-dd","deployStatus":3,"expireTime":180,"expression":"cn_arius_stats_index_info*","id":1067,"ingestPipeline":"cn_arius_stats_index_info","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_arius_stats_index_info"},"slaveInfos":[]},"cn_record.arius.template.value":{"baseInfo":{"aliases":[],"dateField":"timestamp","dateFormat":"_yyyy-MM","deployStatus":3,"expireTime":180,"expression":"cn_record.arius.template.value*","id":1081,"ingestPipeline":"cn_record.arius.template.value","version":0},"masterInfo":{"cluster":"dc-es02","templateName":"cn_record.arius.template.value"},"slaveInfos":[]}},"message":"操作成功"}
    }

    @Test
    public void testListQueryConfig() {
        final DynamicConfigListResponse dynamicConfigListResponse = ariusAdminRemoteService.listQueryConfig();
        System.out.println(JSON.toJSONString(dynamicConfigListResponse));
        assertEquals(dynamicConfigListResponse.getData().size() >= 0, true);
        //{"code":0,"data":[],"message":"操作成功"}
    }

    @Test
    public void testHeartbeat() {
        ariusAdminRemoteService.heartbeat(CLUSTER, "127.0.0.1", 8200);
    }

    @Test
    public void testListDslTemplates() {
        final DSLTemplateListResponse dslTemplateListResponse = ariusAdminRemoteService.listDslTemplates(0L, "");
        System.out.println(JSON.toJSONString(dslTemplateListResponse));
        assertEquals(dslTemplateListResponse.getData() != null, true);
        //TODO:houxiufeng 这里请求arius-admin 返回错误。
    }




}
