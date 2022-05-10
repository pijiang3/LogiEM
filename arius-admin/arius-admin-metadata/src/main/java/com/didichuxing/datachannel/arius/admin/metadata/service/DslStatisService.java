package com.didichuxing.datachannel.arius.admin.metadata.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.AuditDsls;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslQueryLimit;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import com.didiglobal.logi.elasticsearch.client.parser.DslExtractionUtilV2;
import com.didiglobal.logi.elasticsearch.client.parser.bean.ExtractResult;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

@Service
public class DslStatisService {

    protected static final ILog LOGGER = LogFactory.getLog(DslStatisService.class);

    @Autowired
    private DslTemplateESDAO dslTemplateEsDao;

    @Autowired
    private OperateRecordInfoService operateRecordService;

    public Result<String> auditDsl(AuditDsls auditDsls){
        // 入参判断
        if (null == auditDsls || !auditDsls.isVaild()) {
            return Result.build(ResultType.ILLEGAL_PARAMS);
        }

        Integer appid = auditDsls.getAppid();

        List<DslTemplatePO> dslTemplatePOList = Lists.newArrayList();
        boolean auditResult = auditDsl(appid, auditDsls.getDslInfos(), dslTemplatePOList);
        if (!auditResult) {
            return Result.build(ResultType.FAIL);
        }

        boolean operatorResult = dslTemplateEsDao.updateTemplates(dslTemplatePOList);

        // 添加操作记录
        if (operatorResult) {
            for (DslTemplatePO dslTemplatePo : dslTemplatePOList) {
                OperateRecordDTO operateRecord = buildDslSettingOperatorRecord(
                        String.format("%d_%s", appid, dslTemplatePo.getDslTemplateMd5()), OperationEnum.EDIT.getCode(), auditDsls.getUserName(),
                        String.format("checkMode->%s", dslTemplatePo.getCheckMode()));
                operateRecordService.save(operateRecord);
            }
        }

        return Result.build(operatorResult);
    }

    public Result<Boolean> batchUpdateQueryLimit(List<DslQueryLimit> dslQueryLimitList, String operator) {
        if (CollectionUtils.isEmpty(dslQueryLimitList)) {
            return Result.build(ResultType.ILLEGAL_PARAMS);
        }

        // 获取到原来的查询模板信息
        Map<String, DslTemplatePO> orginalMap = dslTemplateEsDao.getDslTemplateByKeys(dslQueryLimitList);

        // 更新查询限流值
        boolean operatorResult = dslTemplateEsDao.updateQueryLimitByAppidDslTemplate(dslQueryLimitList);
        if (!operatorResult) {
            return Result.build(ResultType.FAIL.getCode(), "document missing fail to update query limit");
        }

        DslTemplatePO defaultDsl = new DslTemplatePO();
        defaultDsl.setQueryLimit(0D);

        // 添加操作记录
        for (DslQueryLimit dslQueryLimit : dslQueryLimitList) {
            OperateRecordDTO operateRecord = buildDslSettingOperatorRecord(
                    dslQueryLimit.getAppidDslTemplateMd5(), OperationEnum.EDIT.getCode(), operator,
                    String.format("queryLimit %f->%f",
                            orginalMap.getOrDefault(dslQueryLimit.getAppidDslTemplateMd5(), defaultDsl).getQueryLimit(),
                            dslQueryLimit.getQueryLimit())
            );
            operateRecordService.save(operateRecord);
        }

        return Result.buildSucc(true);
    }

    /**
     * 滚动获取查询模板数据
     * @param request
     * @return
     */
    public Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(ScrollDslTemplateRequest request) {
        if (request == null || !request.isValid()) {
            return Result.buildParamIllegal("参数非法");
        }

        try {
            ScrollDslTemplateResponse response = dslTemplateEsDao.handleScrollDslTemplates(request);
            if (response == null) {
                return Result.buildFail("查询es失败");
            }

            return Result.buildSucc(response);

        } catch (Exception e) {
            LOGGER.error("class=DslAnalyzerController||method=scrollSearchDslTemplate||errMsg=request {}, search es error||stack={}",
                    request, e);
            return Result.buildFail();
        }
    }

    /********************************************* private methods *********************************************/
    /**
     * DSL审核查询语句
     *
     * @param appid
     * @param dslInfos
     * @param dslTemplatePOList
     * @return
     */
    private boolean auditDsl(Integer appid, List<DslInfo> dslInfos, List<DslTemplatePO> dslTemplatePOList) {

        String dsl = null;
        ExtractResult extractResult = null;
        String dslTemplateMd5 = null;
        DslTemplatePO dslTemplatePO = null;
        String timeValue = DateTimeUtil.getCurrentFormatDateTime();

        for (DslInfo dslInfo : dslInfos) {

            if (StringUtils.isBlank(dslInfo.getDsl())) {
                continue;
            }

            dsl = dslInfo.getDsl();
            // 对查询语句进行提取成模板
            extractResult = DslExtractionUtilV2.extractDsl(dsl);
            if (null == extractResult) {
                LOGGER.error("class=DslStatisService||method=auditDsl||msg=auditDsl extractDsl {} FAIL", dsl);
                continue;
            }

            // 对新版本进行审核
            dslTemplateMd5 = extractResult.getDslTemplateMd5();

            dslTemplatePO = dslTemplateEsDao.getDslTemplateByKey(appid, dslTemplateMd5);

            // 如果为空，则新建索引模板
            if (null == dslTemplatePO) {
                dslTemplatePO = createDslTemplate(appid, dsl, extractResult, dslTemplateMd5, timeValue);

                // 如果存在，就修改黑白名单和修改时间
            } else {
                dslTemplatePO.setCheckMode("white");
                dslTemplatePO.setAriusModifyTime(timeValue);
            }

            dslTemplatePOList.add(dslTemplatePO);
        }

        // 数据量不等，则失败
        return  dslInfos.size() == dslTemplatePOList.size();
    }

    private DslTemplatePO createDslTemplate(Integer appid, String dsl, ExtractResult extractResult, String dslTemplateMd5, String timeValue) {
        DslTemplatePO dslTemplatePO;
        dslTemplatePO = new DslTemplatePO();
        dslTemplatePO.setVersion("V2");
        dslTemplatePO.setAriusCreateTime(timeValue);
        dslTemplatePO.setAriusModifyTime(timeValue);
        dslTemplatePO.setFlinkTime(timeValue);
        dslTemplatePO.setAppid(appid);
        dslTemplatePO.setDslTemplate(extractResult.getDslTemplate());
        dslTemplatePO.setDslTemplateMd5(dslTemplateMd5);
        dslTemplatePO.setDslType(extractResult.getDslType());
        dslTemplatePO.setSearchType(extractResult.getSearchType());
        dslTemplatePO.setDsl(dsl);

        // 如果查询语句中有聚合查询，或者没有过滤条件，则加入到黑名单
        if ("aggs".equals(extractResult.getDslType()) || StringUtils.isBlank(extractResult.getWhereFields())) {
            dslTemplatePO.setCheckMode("black");
            LOGGER.error("class=DslAnalyzerController||method=auditDsl||errMsg={} {} has aggs or no where", appid, dsl);
            // 通过sql查询语句中有订单号order_id 但没有routing值，则加入到黑名单
        } else if ("sql".equals(extractResult.getSearchType()) &&
                extractResult.getWhereFields().contains("order_id") && !extractResult.getDslTemplate().contains("ROUTINGS")) {
            dslTemplatePO.setCheckMode("black");
            LOGGER.error("class=DslAnalyzerController||method=auditDsl||errMsg={} {} search with order_id no routing", appid, dsl);
        } else {
            dslTemplatePO.setCheckMode("white");
        }

        dslTemplatePO.setQueryLimit(10.0);
        return dslTemplatePO;
    }

    public OperateRecordDTO buildDslSettingOperatorRecord(String bizId, Integer operateId, String operator, String content) {
        OperateRecordDTO operateRecord = new OperateRecordDTO();
        operateRecord.setBizId(bizId);
        operateRecord.setModuleId( ModuleEnum.DSL_ANALYZER.getCode());
        operateRecord.setOperateId(operateId);
        operateRecord.setOperator(operator);
        operateRecord.setContent(content);

        return operateRecord;
    }
}
