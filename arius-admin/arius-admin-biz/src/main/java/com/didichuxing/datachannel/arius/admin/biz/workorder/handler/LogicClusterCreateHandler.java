package com.didichuxing.datachannel.arius.admin.biz.workorder.handler;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum.WORK_ORDER_LOGIC_CLUSTER_CREATE;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterDeleteOrderDetail;
import java.util.Arrays;
import java.util.List;

import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.BaseWorkOrderHandler;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.LogicClusterCreateContent;
import com.didichuxing.datachannel.arius.admin.biz.workorder.notify.LogicClusterCreateNotify;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.workorder.WorkOrderTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.WorkOrder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.AbstractOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail.LogicClusterCreateOrderDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;

/**
 * @author d06679
 * @date 2019/4/29
 */
@Service("logicClusterCreateHandler")
public class LogicClusterCreateHandler extends BaseWorkOrderHandler {

    @Autowired
    private ESClusterLogicService esClusterLogicService;

    @Override
    public AbstractOrderDetail getOrderDetail(String extensions) {
        LogicClusterDeleteOrderDetail content = JSON.parseObject(extensions, LogicClusterDeleteOrderDetail.class);

        return ConvertUtil.obj2Obj(content, LogicClusterCreateOrderDetail.class);
    }

    @Override
    public List<AriusUserInfo> getApproverList(AbstractOrderDetail detail) {
        return getOPList();
    }

    @Override
    public Result checkAuthority(WorkOrderPO orderPO, String userName) {
        if (isOP(userName)) {
            return Result.buildSucc(true);
        }
        return Result.buildFail(ResultType.OPERATE_FORBIDDEN_ERROR.getMessage());
    }

    /**
     * 工单是否自动审批
     *
     * @param workOrder 工单类型
     * @return result
     */
    @Override
    public boolean canAutoReview(WorkOrder workOrder) {
        return false;
    }

    /**************************************** protected method ******************************************/

    /**
     * 验证用户提供的参数
     *
     * @param workOrder 工单
     * @return result
     */
    @Override
    protected Result validateConsoleParam(WorkOrder workOrder) {
        LogicClusterCreateContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterCreateContent.class);

        ESLogicClusterDTO resourceLogicDTO = ConvertUtil.obj2Obj(content, ESLogicClusterDTO.class);
        resourceLogicDTO.setAppId(workOrder.getSubmitorAppid());
        resourceLogicDTO.setType(ResourceLogicTypeEnum.PRIVATE.getCode());
        return esClusterLogicService.validateLogicClusterParams(resourceLogicDTO, OperationEnum.ADD);
    }

    @Override
    protected String getTitle(WorkOrder workOrder) {
        LogicClusterCreateContent content = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            LogicClusterCreateContent.class);

        WorkOrderTypeEnum workOrderTypeEnum = WorkOrderTypeEnum.valueOfName(workOrder.getType());
        if (workOrderTypeEnum == null) {
            return "";
        }
        return content.getName() + workOrderTypeEnum.getMessage();
    }

    /**
     * 验证用户是否有该工单权限
     *
     * @param workOrder 工单内容
     * @return result
     */
    @Override
    protected Result validateConsoleAuth(WorkOrder workOrder) {
        return Result.buildSucc();
    }

    /**
     * 处理工单 该工单需要运维人员在运维控制台处理好后;所以这里不用执行任何逻辑
     */
    @Override
    protected Result doProcessAgree(WorkOrder workOrder, String approver) {
        ESLogicClusterWithRegionDTO esLogicClusterWithRegionDTO = ConvertUtil.obj2ObjByJSON(workOrder.getContentObj(),
            ESLogicClusterWithRegionDTO.class);
        esLogicClusterWithRegionDTO.setAppId(workOrder.getSubmitorAppid());

        Result<Long> result = esClusterLogicService.createLogicCluster(esLogicClusterWithRegionDTO,
            workOrder.getSubmitor());

        if (result.success()) {
            sendNotify(WORK_ORDER_LOGIC_CLUSTER_CREATE, new LogicClusterCreateNotify(workOrder.getSubmitorAppid(),
                esLogicClusterWithRegionDTO.getName(), approver), Arrays.asList(workOrder.getSubmitor()));

            List<String> administrators = getOPList().stream().map(AriusUserInfo::getName).collect(Collectors.toList());
            return Result.buildSucc(
                String.format("请联系管理员【%s】进行后续操作", administrators.get(new Random().nextInt(administrators.size()))));
        }

        return result;
    }

    @Override
    protected Result validateParam(WorkOrder workOrder) {
        return Result.buildSucc();
    }
}
