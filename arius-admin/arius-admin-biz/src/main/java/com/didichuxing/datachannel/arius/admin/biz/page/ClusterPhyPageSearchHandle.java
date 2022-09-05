package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.SortTermEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.remote.zeus.ZeusClusterRemoteService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.didichuxing.datachannel.arius.admin.remote.zeus.ZeusClusterRemoteServiceImpl.*;

/**
 *
 * @author ohushenglin_v
 * @date 2022-05-27
 */
@Component
public class ClusterPhyPageSearchHandle extends AbstractPageSearchHandle<ClusterPhyConditionDTO, ClusterPhyVO> {

    private static final ILog           LOGGER         = LogFactory.getLog(ClusterPhyPageSearchHandle.class);
    private static final CharSequence[] CHAR_SEQUENCES = { "*", "?" };
    public static final Cache</*zeus_agents_list*/String, /*agents_list*/ List<String>> ZEUS_AGENTS_LIST_CACHE = CacheBuilder.newBuilder().build();
    public static final String ZEUS_AGENTS_LIST = "zeus_agents_list";

    @Autowired
    private ClusterPhyService           clusterPhyService;
    @Autowired
    private ClusterLogicService         clusterLogicService;
    @Autowired
    private ClusterPhyManager           clusterPhyManager;
    @Autowired
    private ClusterRegionService        clusterRegionService;

    @Autowired
    private ZeusClusterRemoteService zeusClusterRemoteService;

    @Override
    protected Result<Boolean> checkCondition(ClusterPhyConditionDTO condition, Integer projectId) {

        Integer status = condition.getHealth();
        if (null != status && !ClusterHealthEnum.isExitByCode(status)) {
            return Result.buildParamIllegal("集群状态类型不存在");
        }

        String clusterPhyName = condition.getCluster();
        if (StringUtils.containsAny(clusterPhyName, CHAR_SEQUENCES)) {
            return Result.buildParamIllegal("物理集群名称不允许带类似*, ?等通配符查询");
        }

        if (null != condition.getSortTerm() && !SortTermEnum.isExit(condition.getSortTerm())) {
            return Result.buildParamIllegal(String.format("暂且不支持排序字段[%s]", condition.getSortTerm()));
        }

        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(ClusterPhyConditionDTO condition, Integer projectId) {
        List<String> clusterNames = null;
        Integer queryProjectId = null;
        String logicClusterName = condition.getLogicClusterName();
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            // 非超级管理员，获取拥有的逻辑集群对应的物理集群列表
            queryProjectId = projectId;
        }
        List<ClusterLogic> clusterLogicList = null;
        if (StringUtils.isNotBlank(logicClusterName) || null != queryProjectId) {
            //根据项目ID或者集群名称获取逻辑集群，当项目为超级项目时不需要传入项目ID
            clusterLogicList = clusterLogicService.listClusterLogicByProjectIdAndName(queryProjectId, logicClusterName);
            clusterNames = Lists.newArrayList();
        }
        if (CollectionUtils.isNotEmpty(clusterLogicList)) {
            //项目下的有管理权限逻辑集群会关联多个物理集群
            List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(
                clusterLogicList.stream().map(ClusterLogic::getId).collect(Collectors.toList()));
            clusterNames = regions.stream().map(ClusterRegion::getPhyClusterName).distinct()
                .collect(Collectors.toList());
        }
        if (null != clusterNames) {
            //这里默认给个空字符串，确保没有查询到逻辑集群的查询条件下无法查询出数据
            clusterNames.add("");
            condition.setClusterNames(clusterNames);
        }
        String sortTerm = null == condition.getSortTerm() ? SortConstant.ID : condition.getSortTerm();
        String sortType = condition.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        condition.setSortTerm(sortTerm);
        condition.setSortType(sortType);
        condition.setFrom((condition.getPage() - 1) * condition.getSize());
    }

    @Override
    protected PaginationResult<ClusterPhyVO> buildPageData(ClusterPhyConditionDTO condition, Integer projectId) {

        List<ClusterPhy> pagingGetClusterPhyList = clusterPhyService.pagingGetClusterPhyByCondition(condition);

        List<ClusterPhyVO> clusterPhyVOList = clusterPhyManager.buildClusterInfo(pagingGetClusterPhyList);

        long totalHit = clusterPhyService.fuzzyClusterPhyHitByCondition(condition);
        List<ClusterPhyVO> clusterPhyList = clusterPhyVOList.stream()
            .filter(clusterPhyVO -> ClusterHealthEnum.UNKNOWN.getCode().equals(clusterPhyVO.getHealth()))
            .collect(Collectors.toList());
        //非正常集群需要重新发事件

        for (ClusterPhyVO clusterPhyVO : clusterPhyList) {
            SpringTool.publish(new ClusterPhyEvent(clusterPhyVO.getCluster(), AriusUser.SYSTEM.getDesc()));
        }
        List<String> ipList = ipListWithCache();
        //判断集群是否支持zeus，并设置对应的参数值
        for (ClusterPhyVO clusterPhyVO :clusterPhyVOList) {
            buildSupportZeusByClusterPhy(clusterPhyVO,ipList);
        }
        return PaginationResult.buildSucc(clusterPhyVOList, totalHit, condition.getPage(), condition.getSize());
    }

    /**
     * > 该函数用于获取存储zeus部署的ip列表的缓存
     *return List<String>
     */
    private List<String> ipListWithCache() {
        //捕获缓存中的异常
        try {
            return ZEUS_AGENTS_LIST_CACHE.get(ZEUS_AGENTS_LIST, () -> getIpList());
        } catch (Exception e) {
            LOGGER.error(
                    "class=ClusterPhyPageSearchHandle||method=ipListWithCache||error={}", e.getMessage());
            return getIpList();
        }
    }

    /**
     * > 该函数用于缓存初次获取zeus部署的agents list
     *return List<String>
     */
    private List<String> getIpList() {
        Result<List<String>> result = zeusClusterRemoteService.getAgentsList();
        //如果获取zeus失败则返回空列表
        if (result.failed()) {
            return Lists.newArrayList();
        } else {
            return result.getData();
        }
    }

    /**
     * > 该函数用于构建支持zeus by cluster phy
     *
     * @param clusterPhyVO 集群物理信息
     */
    private void buildSupportZeusByClusterPhy(ClusterPhyVO clusterPhyVO,List<String> zeusAgentsList) {
        List<ClusterRoleHost> clusterRoleHosts = clusterPhyManager.listClusterRoleHostByCluster(clusterPhyVO.getCluster());
        Boolean supportZeus = true ;
        for (ClusterRoleHost clusterRoleHost : clusterRoleHosts) {
            if (!zeusAgentsList.contains(clusterRoleHost.getIp())) {
                supportZeus = false;
            }
        }
        clusterPhyVO.setSupportZeus(supportZeus);
    }
}