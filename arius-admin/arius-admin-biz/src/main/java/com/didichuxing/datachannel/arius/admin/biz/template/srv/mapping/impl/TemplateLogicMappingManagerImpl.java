package com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.impl;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.precreate.TemplatePreCreateManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.MappingOptimize;
import com.didichuxing.datachannel.arius.admin.client.bean.common.MappingOptimizeItem;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateSchemaDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.ConsoleTemplateSchemaOptimizeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.mapping.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.*;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateTypePO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusIndexMappingConfigUtils;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl.TemplateLogicServiceImpl;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateSattisService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateConfigDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateTypeDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.MappingConfig;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.TypeConfig;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.TypeDefine;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.TypeProperties;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.EDIT_TEMPLATE_MAPPING;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.DEFAULT_TYPE;
import static com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum.TEMPLATE_MAPPING;

/**
 * @author zhonghua
 * @date 2019-06-13
 */
@Service
public class TemplateLogicMappingManagerImpl extends BaseTemplateSrv implements TemplateLogicMappingManager {

    private static final ILog              LOGGER = LogFactory.getLog(TemplateLogicServiceImpl.class);

    @Autowired
    private TemplatePhyMappingManager templatePhyMappingManager;

    @Autowired
    private TemplateSattisService           templateSattisService;

    @Autowired
    private TemplatePreCreateManager templatePreCreateManager;

    @Autowired
    private IndexTemplateConfigDAO         templateConfigDAO;

    @Autowired
    private IndexTemplateTypeDAO           indexTemplateTypeDAO;

    @Override
    public TemplateServiceEnum templateService() {
        return TEMPLATE_MAPPING;
    }

    /**
     * 查询指定的逻辑模板的Field信息
     *
     * @param logicId 模板id
     * @return 模板信息  不存在返回null
     */
    @Override
    public Result<IndexTemplateLogicWithMapping> getTemplateWithMapping(Integer logicId) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            LOGGER.warn("method=getTemplateWithFieldById||msg=not exit||logicId={}", logicId);
            return Result.buildFrom(Result.buildNotExist("模板不存在"));
        }

        IndexTemplateLogicWithMapping templateLogicWithMapping = ConvertUtil.obj2Obj(templateLogicWithPhysical,
            IndexTemplateLogicWithMapping.class);
        if (templateLogicWithPhysical.hasPhysicals()) {
            MappingConfig mergeMappingConfig = null;
            List<IndexTemplatePhy> masterPhysicalTemplates = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
            for (IndexTemplatePhy templatePhysical : masterPhysicalTemplates) {

                Result<MappingConfig> result = templatePhyMappingManager.getMapping(templatePhysical.getCluster(),
                    templatePhysical.getName());
                if (result.failed()) {
                    return Result.buildFrom(result);
                }
                MappingConfig mappingConfig = result.getData();
                if (mergeMappingConfig == null) {
                    mergeMappingConfig = mappingConfig;
                } else {
                    mergeMappingConfig.merge(mappingConfig);
                }
            }

            templateLogicWithMapping
                .setTypeProperties(genAriusTypePropertyList(templateLogicWithMapping, mergeMappingConfig.getMapping()));
            List<Field> fields = null;
            try {
                fields = convert2Fields(mergeMappingConfig);
            } catch (Throwable t) {
                LOGGER.warn("method=getTemplateWithFieldById||msg=mappint to field error||logicId={}", logicId, t);
            }
            if (fields == null) {
                fields = new ArrayList<>();
            }
            templateLogicWithMapping.setFields(fields);
        } else {
            LOGGER.warn("method=getTemplateWithFieldById||msg=not deploy||logicId={}", logicId);
        }

        return Result.buildSucc(templateLogicWithMapping);
    }

    @Override
    public Result checkFields(Integer logicId, List<Field> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return Result.buildSucc();
        }

        Result result = checkFieldInternal(fields);
        if (result.failed()) {
            return result;
        }

        List<IndexTemplatePhy> templatePhysicals = getMasterTemplatePhysicalByLogicId(logicId);

        if (templatePhysicals == null) {
            return Result.buildFail("can not find template physical, logicId:" + logicId);
        }

        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            result = getDiffMapping(templatePhysical.getCluster(), templatePhysical.getName(), fields);
            if (result.failed()) {
                return result;
            }

            MappingConfig mappingConfig = (MappingConfig) result.getData();

            String cluster = templatePhysical.getCluster();
            String template = templatePhysical.getName();
            String mapping = mappingConfig.toJson().toJSONString();
            result = templatePhyMappingManager.checkMapping(cluster, template, mapping, true);
            if (result.failed()) {
                return result;
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result updateFields(Integer logicId, List<Field> fields, Set<String> removeFields) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }

        Result checkFieldResult = checkFieldInternal(fields);
        if (checkFieldResult.failed()) {
            return checkFieldResult;
        }

        List<IndexTemplatePhy> masterPhysicalTemplates = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy masterTemplatePhysical : masterPhysicalTemplates) {
            if (masterTemplatePhysical == null) {
                return Result.buildFail("can not find template physical, logicId:" + logicId);
            }

            Result getDiffResult = getDiffMapping(masterTemplatePhysical.getCluster(), masterTemplatePhysical.getName(),
                fields);
            if (getDiffResult.failed()) {
                return getDiffResult;
            }

            String diffMapping = ((MappingConfig) getDiffResult.getData()).toJson().toJSONString();

            Result editResult = templatePhyMappingManager.updateMappingAndMerge(
                masterTemplatePhysical.getCluster(), masterTemplatePhysical.getName(), diffMapping, removeFields);
            if (editResult.failed()) {
                return editResult;
            }
        }

        return Result.buildSucc();
    }

    @Override
    public AriusTypeProperty fields2Mapping(List<Field> fields) {

        if (fields == null) {
            fields = Lists.newArrayList();
        }

        MappingConfig mappingConfig = convert2Mapping(AdminConstant.DEFAULT_TYPE, fields);
        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        ariusTypeProperty.setTypeName(AdminConstant.DEFAULT_TYPE);
        if (CollectionUtils.isNotEmpty(fields)) {
            ariusTypeProperty
                .setProperties(mappingConfig.getMapping().get(AdminConstant.DEFAULT_TYPE).getProperties().toJson());
        } else {
            ariusTypeProperty.setProperties(new JSONObject());
        }
        return ariusTypeProperty;
    }

    /**
     * 获取mapping优化信息
     *
     * @param logicId logicId
     * @return result
     */
    @Override
    public Result<List<MappingOptimize>> getTemplateMappingOptimize(Integer logicId) {
        IndexTemplateLogicWithPhyTemplates logicWithPhysical = this.templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (logicWithPhysical == null) {
            return Result.buildFrom(Result.buildNotExist("模板不存在"));
        }

        List<IndexTemplatePhy> templatePhysicals = logicWithPhysical.fetchMasterPhysicalTemplates();

        List<MappingOptimize> mappingOptimizes = new ArrayList<>();
        for (IndexTemplatePhy master : templatePhysicals) {
            Result<MappingOptimize> getMappingOptimizeResult = templateSattisService
                .getMappingOptimize(master.getCluster(), master.getName());

            if (getMappingOptimizeResult.failed()) {
                return Result.buildFrom(getMappingOptimizeResult);
            }

            if (!CollectionUtils.isEmpty(getMappingOptimizeResult.getData().getOptimizeItems())) {
                mappingOptimizes.add(getMappingOptimizeResult.getData());
            }
        }

        if (CollectionUtils.isEmpty(mappingOptimizes)) {
            return Result.buildFrom(Result.buildParamIllegal("mapping不需要优化"));
        }

        return Result.buildSucc(mappingOptimizes);

    }

    /**
     * mapping优化
     *
     * @param optimizeDTO dto
     * @param operator    操作人
     * @return result
     */
    @Override
    public Result modifySchemaOptimize(ConsoleTemplateSchemaOptimizeDTO optimizeDTO, String operator) {

        if (CollectionUtils.isEmpty(optimizeDTO.getItems())) {
            return Result.buildParamIllegal("未选择优化字段");
        }

        List<AriusTypeProperty> typeProperties = buildAriusTypeProperty(optimizeDTO.getItems());

        return updateProperties(optimizeDTO.getLogicId(), typeProperties);
    }

    @Override
    public Result updateMappingForNew(Integer logicId, AriusTypeProperty ariusTypeProperty) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }

        TemplateConfigPO config = templateConfigDAO.getByLogicId(logicId);

        List<IndexTemplatePhy> templatePhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {

            Result mappingConfigResult = AriusIndexMappingConfigUtils
                .parseMappingConfig(ariusTypeProperty.toMappingJSON().toJSONString());
            if (mappingConfigResult.failed()) {
                return mappingConfigResult;
            }

            MappingConfig mappingConfig = (MappingConfig) mappingConfigResult.getData();

            if (config != null && config.getDisableSourceFlags() != null && config.getDisableSourceFlags()) {
                LOGGER.info("method=updateMappingForNew||msg=disableSource||logicId={}", logicId);
                mappingConfig.disableSource();
            } else {
                mappingConfig.enableSource();
            }

            Result result = templatePhyMappingManager.updateMapping(templatePhysical.getCluster(),
                templatePhysical.getName(), mappingConfig.toJson().toJSONString());

            if (result.failed()) {
                return result;
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result updateProperties(Integer logicId, List<AriusTypeProperty> ariusTypePropertyList) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            return Result.buildNotExist("逻辑模板不存在, ID:" + logicId);
        }

        if (!templateLogicWithPhysical.hasPhysicals()) {
            return Result.buildNotExist("物理模板不存在，ID:" + logicId);
        }

        TemplateConfigPO config = templateConfigDAO.getByLogicId(logicId);

        List<IndexTemplatePhy> templatePhysicals = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            Result result = templatePhyMappingManager.getMapping(templatePhysical.getCluster(),
                templatePhysical.getName());
            if (result.failed()) {
                return result;
            }
            MappingConfig templateMappingConfig = (MappingConfig) (result.getData());
            Map<String, TypeConfig> typeConfigMap = templateMappingConfig.getMapping();

            if (typeConfigMap.size() == 0) {
                TypeConfig typeConfig = new TypeConfig();
                typeConfigMap.put(AdminConstant.DEFAULT_TYPE, typeConfig);
            }

            // 将对应的type替换
            for (AriusTypeProperty ariusTypeProperty : ariusTypePropertyList) {
                //高版本es集群只有一个type
                TypeProperties typeProperties = new TypeProperties(ariusTypeProperty.getProperties());

                for (String type : typeConfigMap.keySet()) {
                    typeConfigMap.get(type).setProperties(typeProperties);
                }
            }

            if (config != null && config.getDisableSourceFlags()) {
                templateMappingConfig.disableSource();
            } else {
                templateMappingConfig.enableSource();
            }

            result = templatePhyMappingManager.updateMapping(templatePhysical.getCluster(),
                templatePhysical.getName(), templateMappingConfig.toJson().toJSONString());

            if (result.failed()) {
                return result;
            }
        }

        return Result.buildSucc();
    }

    /**
     * 修改模板schema
     *
     * @param schemaDTO schema
     * @param operator  操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result modifySchema(ConsoleTemplateSchemaDTO schemaDTO, String operator) throws AdminOperateException {
        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人为空");
        }

        if (schemaDTO.getFields() == null && schemaDTO.getTypeProperties() == null) {
            return Result.buildParamIllegal("fields或mapping信息非法");
        }

        if (schemaDTO.getFields() != null && schemaDTO.getTypeProperties() != null) {
            return Result.buildParamIllegal("fields或mapping信息非法");
        }

        if (schemaDTO.getFields() != null) {
            if (!clusterIsHighVersion(schemaDTO.getLogicId())) {
                return Result.buildParamIllegal("该功能只支持高高版本es， 请使用JSON格式");
            }
        }

        Result saveSpecialFieldResult = saveSpecialField(schemaDTO, operator);
        if (saveSpecialFieldResult.failed()) {
            return saveSpecialFieldResult;
        }

        // 修改mapping信息.
        if (schemaDTO.getTypeProperties() != null) {
            Result result = updateProperties(schemaDTO.getLogicId(), schemaDTO.getTypeProperties());
            if (result.success()) {
                templatePreCreateManager.reBuildTomorrowIndex(schemaDTO.getLogicId(), 3);
            }
            return result;
        }

        Result result = updateFields(schemaDTO.getLogicId(), schemaDTO.getFields(), schemaDTO.getRemoveFieldNames());
        if (result.success()) {
            // 记录操作记录
            operateRecordService.save(TEMPLATE, EDIT_TEMPLATE_MAPPING, schemaDTO.getLogicId(), "-", operator);

            // 重建明天索引
            templatePreCreateManager.reBuildTomorrowIndex(schemaDTO.getLogicId(), 3);
        }

        return result;
    }

    /**************************************** private method ****************************************************/
    private List<AriusTypeProperty> genAriusTypePropertyList(IndexTemplateLogic templateLogic,
                                                             Map<String, TypeConfig> typeConfigMap) {

        List<IndexTemplateType> templateTypes = templateLogicService.getLogicTemplateTypes(templateLogic.getId());
        Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap = ConvertUtil.list2Map(templateTypes,
            IndexTemplateType::getName);

        List<AriusTypeProperty> ariusTypePropertyList = Lists.newArrayList();

        if (typeConfigMap == null || typeConfigMap.size() == 0) {
            ariusTypePropertyList.add(buildDefaultType(templateLogic));
        } else if (typeConfigMap.size() == 1) {
            ariusTypePropertyList.add(buildOneType(templateLogic, typeConfigMap, typeName2IndexTemplateTypeMap));
        } else {
            List<AriusTypeProperty> typeProperties = buildMultiType(templateLogic, typeConfigMap,
                typeName2IndexTemplateTypeMap);
            ariusTypePropertyList.addAll(typeProperties);
        }

        return ariusTypePropertyList;
    }

    private List<IndexTemplatePhy> getMasterTemplatePhysicalByLogicId(Integer logicId) {
        IndexTemplateLogicWithPhyTemplates templateLogicWithPhysical = templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null) {
            LOGGER.warn("method=getIndexTemplatePhysicalByLogicId||msg=not exit||logicId={}", logicId);
            return null;
        }

        if (templateLogicWithPhysical.hasPhysicals()) {
            return templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        } else {
            LOGGER.warn("method=getIndexTemplatePhysicalByLogicId||msg=not deploy||logicId={}", logicId);
            return null;
        }
    }

    private Result checkFieldInternal(List<Field> fields) {
        Set<String> existName = Sets.newHashSet();
        for (Field field : fields) {
            if (AriusObjUtils.isNull(field)) {
                return Result.buildParamIllegal("字段信息不能为空");
            }

            if (AriusObjUtils.isNull(field.getName())) {
                return Result.buildParamIllegal("字段名称不能为空");
            }
            if (existName.contains(field.getName())) {
                return Result.buildParamIllegal("字段名称重复");
            }
            existName.add(field.getName());

            if (AriusObjUtils.isNull(field.getType())) {
                return Result.buildParamIllegal("字段类型不能为空");
            }
            TypeEnum typeEnum = TypeEnum.valueFrom(field.getType());
            if (typeEnum.equals(TypeEnum.UNKNOWN)) {
                return Result.buildParamIllegal("字段类型非法");
            }

            if (AriusObjUtils.isNull(field.getIndexType())) {
                return Result.buildParamIllegal("检索类型不能为空");
            }
            IndexEnum indexEnum = IndexEnum.valueFrom(field.getIndexType());
            if (indexEnum.equals(IndexEnum.UNKNOWN)) {
                return Result.buildParamIllegal("检索类型非法");
            }

            if (field.getAnalyzerType() != null) {
                AnalyzerEnum analyzerEnum = AnalyzerEnum.valueFrom(field.getAnalyzerType());
                if (analyzerEnum.equals(AnalyzerEnum.UNKNOWN)) {
                    return Result.buildParamIllegal("分词器非法");
                }
            }

            if (field.getSortType() != null) {
                SortEnum sortEnum = SortEnum.valueFrom(field.getSortType());
                if (sortEnum.equals(SortEnum.UNKNOWN)) {
                    return Result.buildParamIllegal("是否排序非法非法");
                }
            }

            if (field.getType().equals(TypeEnum.STRING.getCode())) {
                if (field.getIndexType().equals(IndexEnum.FUZZY.getCode())) {
                    if (field.getSortType() != null && field.getSortType().equals(SortEnum.YES.getCode())) {
                        return Result.buildParamIllegal("全文检索的string类型不能排序");
                    }
                    if (field.getAnalyzerType() == null) {
                        return Result.buildParamIllegal("全文检索的string类型必须选择分词器");
                    }
                }

                if (field.getIndexType().equals(IndexEnum.FORBID.getCode())) {
                    if (field.getAnalyzerType() != null) {
                        return Result.buildParamIllegal("不检索的string类型不能配置分词器");
                    }
                }

                if (field.getIndexType().equals(IndexEnum.EXACT.getCode())) {
                    if (field.getAnalyzerType() != null) {
                        return Result.buildParamIllegal("精确检索的string类型不能配置分词器");
                    }
                }
            } else {
                if (field.getIndexType().equals(IndexEnum.FUZZY.getCode())) {
                    return Result.buildParamIllegal("非string类型的字段不能配置全文检索");
                }
                if (field.getAnalyzerType() != null) {
                    return Result.buildParamIllegal("非string类型的字段不能配置分词器");
                }
            }
        }

        return Result.buildSucc();
    }

    /**
     * 根据现有配置 和 fields对比，得到修改的mapping
     */
    private Result<MappingConfig> getDiffMapping(String cluster, String template, List<Field> fields) {
        try {
            Result result = templatePhyMappingManager.getMapping(cluster, template);
            if (result.failed()) {
                return result;
            }

            MappingConfig mappingConfig = (MappingConfig) result.getData();
            Set<String> typeNames = mappingConfig.getMapping().keySet();
            if (typeNames.size() > 1) {
                return Result
                    .buildFail("模板中有多个type，请使用mapping json的方式修改, " + "cluster:" + cluster + ", template:" + template);
            }

            String type = "type";
            if (typeNames.size() == 1) {
                for (String typeName : typeNames) {
                    type = typeName;
                    break;
                }
            }

            List<Field> srcFields = convert2Fields(mappingConfig);

            List<Field> diffFields = diffField(srcFields, fields);

            mappingConfig = convert2Mapping(type, diffFields);

            return Result.buildSucc(mappingConfig);

        } catch (Throwable t) {
            return Result.buildFail(t.getMessage());
        }
    }

    private static final String TEXT_STR       = "text";
    private static final String TYPE_STR       = "type";
    private static final String KEYWORD_STR    = "keyword";
    private static final String IK_SMART_SRT   = "ik_smart";
    private static final String ANALYZER_STR   = "analyzer";
    private static final String DOC_VALUES_STR = "doc_values";
    private static final String INDEX_STR      = "index";

    private List<Field> convert2Fields(MappingConfig mapping) {
        Map<String/*typeName*/, Map<String/*field*/, TypeDefine>> typeFieldTypeMap = mapping.getTypeDefines();
        List<Field> ret = new ArrayList<>();

        Set<String> fieldNames = new HashSet<>();

        for (String typeName : typeFieldTypeMap.keySet()) {
            for (String fieldName : typeFieldTypeMap.get(typeName).keySet()) {
                if (fieldNames.contains(fieldName)) {
                    continue;
                } else {
                    fieldNames.add(fieldName);
                }

                TypeDefine td = typeFieldTypeMap.get(typeName).get(fieldName);

                JSONObject typeObj = td.getDefine();
                if (typeObj.isEmpty()) {
                    continue;
                }

                Field field = new Field();
                field.setName(fieldName);
                if (TEXT_STR.equals(typeObj.getString(TYPE_STR))) {
                    field.setType(TypeEnum.STRING.getCode());

                    // 分词配置
                    if (typeObj.containsKey(ANALYZER_STR) && IK_SMART_SRT.equals(typeObj.getString(ANALYZER_STR))) {
                        field.setAnalyzerType(AnalyzerEnum.IK.getCode());
                    } else {
                        field.setAnalyzerType(AnalyzerEnum.DEFAULT.getCode());
                    }

                    if (typeObj.containsKey(INDEX_STR) && !typeObj.getBoolean(INDEX_STR)) {
                        field.setIndexType(IndexEnum.FORBID.getCode());
                        field.setAnalyzerType(null);
                    } else {
                        field.setIndexType(IndexEnum.FUZZY.getCode());
                    }

                    if (typeObj.containsKey(DOC_VALUES_STR)) {
                        if (typeObj.getBoolean(DOC_VALUES_STR)) {
                            field.setSortType(SortEnum.YES.getCode());
                        } else {
                            field.setSortType(SortEnum.NO.getCode());
                        }
                    }

                } else if (KEYWORD_STR.equals(typeObj.get(TYPE_STR))) {
                    field.setType(TypeEnum.STRING.getCode());

                    if (typeObj.containsKey(INDEX_STR) && !typeObj.getBoolean(INDEX_STR)) {
                        field.setIndexType(IndexEnum.FORBID.getCode());
                    } else {
                        field.setIndexType(IndexEnum.EXACT.getCode());
                    }

                    if (typeObj.containsKey(DOC_VALUES_STR)) {
                        if (typeObj.getBoolean(DOC_VALUES_STR)) {
                            field.setSortType(SortEnum.YES.getCode());
                        } else {
                            field.setSortType(SortEnum.NO.getCode());
                        }
                    }

                } else if (TypeEnum.INT.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                           || TypeEnum.LONG.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                           || TypeEnum.Boolean.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                           || TypeEnum.DOUBLE.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                           || TypeEnum.DATE.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))
                           || TypeEnum.OBJECT.getCode().equalsIgnoreCase(typeObj.getString(TYPE_STR))) {
                    field.setType(typeObj.getString(TYPE_STR));

                    if (typeObj.containsKey(INDEX_STR) && !typeObj.getBoolean(INDEX_STR)) {
                        field.setIndexType(IndexEnum.FORBID.getCode());
                    } else {
                        field.setIndexType(IndexEnum.EXACT.getCode());
                    }

                    if (typeObj.containsKey(DOC_VALUES_STR)) {
                        if (typeObj.getBoolean(DOC_VALUES_STR)) {
                            field.setSortType(SortEnum.YES.getCode());
                        } else {
                            field.setSortType(SortEnum.NO.getCode());
                        }
                    }

                } else {
                    field.setType(TypeEnum.UNKNOWN.getCode());
                    field.setAnalyzerType(AnalyzerEnum.UNKNOWN.getCode());
                    field.setIndexType(IndexEnum.UNKNOWN.getCode());
                    field.setSortType(SortEnum.UNKNOWN.getCode());
                }

                ret.add(field);
            }
        }

        if (!EnvUtil.isOnline()) {
            LOGGER.warn("method=convert2Fields||ret={}||mapping={}", ret, mapping);
        }

        return ret;
    }

    private MappingConfig convert2Mapping(String type, List<Field> fields) {
        Map<String, TypeDefine> mappingTypes = new HashMap<>();

        for (Field field : fields) {
            // fields -> type
            TypeEnum typeEnum = TypeEnum.valueFrom(field.getType());
            IndexEnum indexEnum = IndexEnum.valueFrom(field.getIndexType());
            SortEnum sortEnum = SortEnum.valueFrom(field.getSortType());
            AnalyzerEnum analyzerEnum = AnalyzerEnum.valueFrom(field.getAnalyzerType());

            JSONObject typeObj = new JSONObject();
            if (typeEnum == TypeEnum.STRING) {
                if (indexEnum == IndexEnum.FUZZY) {
                    typeObj.put(TYPE_STR, TEXT_STR);
                } else {
                    typeObj.put(TYPE_STR, KEYWORD_STR);
                }

                if (indexEnum == IndexEnum.FORBID) {
                    typeObj.put(INDEX_STR, false);
                } else if (indexEnum == IndexEnum.EXACT) {
                    typeObj.put(INDEX_STR, true);
                }

                if (sortEnum == SortEnum.NO) {
                    typeObj.put(DOC_VALUES_STR, false);
                } else if (sortEnum == SortEnum.YES) {
                    typeObj.put(DOC_VALUES_STR, true);
                }

                if (analyzerEnum == AnalyzerEnum.IK) {
                    typeObj.put(ANALYZER_STR, IK_SMART_SRT);
                }

            } else {
                typeObj.put(TYPE_STR, typeEnum.getCode());

                if (typeEnum != TypeEnum.OBJECT && typeEnum != TypeEnum.ARRAY) {
                    if (indexEnum == IndexEnum.FORBID) {
                        typeObj.put(INDEX_STR, false);
                    } else if (indexEnum == IndexEnum.EXACT) {
                        typeObj.put(INDEX_STR, true);
                    }

                    if (sortEnum == SortEnum.NO) {
                        typeObj.put(DOC_VALUES_STR, false);
                    } else if (sortEnum == SortEnum.YES) {
                        typeObj.put(DOC_VALUES_STR, true);
                    }

                    // 如果新增，或者改为Date类型，则自动增加format
                    if (typeEnum == TypeEnum.DATE) {
                        typeObj.put("format",
                            "yyyy-MM-dd HH:mm:ss Z||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss.SSS Z||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss,SSS||yyyy/MM/dd HH:mm:ss||yyyy-MM-dd HH:mm:ss,SSS Z||yyyy/MM/dd HH:mm:ss,SSS Z||epoch_millis");
                    }
                }
            }

            mappingTypes.put(field.getName(), new TypeDefine(typeObj));
        }

        MappingConfig mappingConfig = new MappingConfig();

        for (String field : mappingTypes.keySet()) {
            List<String> fieldList = new ArrayList<>();
            for (String fieldName : field.split("\\.")) {
                fieldList.add(fieldName);
            }

            mappingConfig.addFields(type, fieldList, mappingTypes.get(field));
        }

        return mappingConfig;
    }

    /**
     * 返回dst中和src中不同的field
     *
     * @param src
     * @param dst
     * @return
     */
    private List<Field> diffField(List<Field> src, List<Field> dst) {
        Map<String, Field> srcMap = new HashMap<>();

        for (Field field : src) {
            srcMap.put(field.getName(), field);
        }

        List<Field> ret = new ArrayList<>();
        for (Field field : dst) {
            if (!srcMap.containsKey(field.getName())) {
                ret.add(field);
                continue;
            }

            if (!field.esTypeEquals(srcMap.get(field.getName()))) {
                ret.add(field);
            }
        }

        return ret;
    }

    private AriusTypeProperty buildDefaultType(IndexTemplateLogic templateLogic) {
        // 优先取模板中的id和routing
        String idField = templateLogic.getIdField();
        String routingField = templateLogic.getRoutingField();

        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        ariusTypeProperty.setTypeName(AdminConstant.DEFAULT_TYPE);

        ariusTypeProperty.setProperties(new JSONObject());

        ariusTypeProperty.setIdField(idField);
        ariusTypeProperty.setRoutingField(routingField);
        ariusTypeProperty.setDateField(templateLogic.getDateField());
        ariusTypeProperty.setDateFieldFormat(templateLogic.getDateFieldFormat());

        return ariusTypeProperty;
    }

    private AriusTypeProperty buildOneType(IndexTemplateLogic templateLogic, Map<String, TypeConfig> typeConfigMap,
                                           Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap) {
        String typeName = typeConfigMap.keySet().iterator().next();

        // 优先取模板中的id和routing
        String idField = templateLogic.getIdField();
        String routingField = templateLogic.getRoutingField();

        if (typeName2IndexTemplateTypeMap.containsKey(typeName)) {
            IndexTemplateType typeFromMysql = typeName2IndexTemplateTypeMap.get(typeName);

            if (StringUtils.isBlank(idField) && typeFromMysql != null) {
                idField = typeFromMysql.getIdField();
            }

            if (StringUtils.isBlank(routingField) && typeFromMysql != null) {
                routingField = typeFromMysql.getRouting();
            }
        }

        TypeConfig typeConfig = typeConfigMap.get(typeName);

        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        ariusTypeProperty.setTypeName(typeName);

        if (typeConfig.getProperties() == null) {
            ariusTypeProperty.setProperties(new JSONObject());
        } else {
            ariusTypeProperty.setProperties(typeConfig.getProperties().toJson());
        }

        ariusTypeProperty.setIdField(idField);
        ariusTypeProperty.setRoutingField(routingField);
        ariusTypeProperty.setDateField(templateLogic.getDateField());
        ariusTypeProperty.setDateFieldFormat(templateLogic.getDateFieldFormat());

        return ariusTypeProperty;
    }

    private List<AriusTypeProperty> buildMultiType(IndexTemplateLogic templateLogic,
                                                   Map<String, TypeConfig> typeConfigMap,
                                                   Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap) {
        List<AriusTypeProperty> typeProperties = Lists.newArrayList();
        for (String typeName : typeConfigMap.keySet()) {
            TypeConfig typeConfig = typeConfigMap.get(typeName);

            AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
            ariusTypeProperty.setTypeName(typeName);

            if (typeConfig.getProperties() == null) {
                ariusTypeProperty.setProperties(new JSONObject());
            } else {
                ariusTypeProperty.setProperties(typeConfig.getProperties().toJson());
            }

            if (typeName2IndexTemplateTypeMap.containsKey(typeName)) {
                IndexTemplateType typeFromMysql = typeName2IndexTemplateTypeMap.get(typeName);
                if (typeFromMysql != null) {
                    ariusTypeProperty.setIdField(typeFromMysql.getIdField());
                    ariusTypeProperty.setRoutingField(typeFromMysql.getRouting());
                    ariusTypeProperty.setDateField(templateLogic.getDateField());
                    ariusTypeProperty.setDateFieldFormat(templateLogic.getDateFieldFormat());
                }
            }

            typeProperties.add(ariusTypeProperty);
        }

        return typeProperties;
    }

    private List<AriusTypeProperty> buildAriusTypeProperty(List<MappingOptimizeItem> items) {
        Multimap<String, MappingOptimizeItem> typeName2JSONObjectMultiMap = ConvertUtil.list2MulMap(items,
            MappingOptimizeItem::getTypeName);

        List<AriusTypeProperty> typeProperties = Lists.newArrayList();
        for (String typeName : typeName2JSONObjectMultiMap.keySet()) {
            AriusTypeProperty typeProperty = new AriusTypeProperty();
            typeProperty.setTypeName(typeName);

            Collection<MappingOptimizeItem> typeOptimizeItems = typeName2JSONObjectMultiMap.get(typeName);
            JSONObject properties = new JSONObject();
            for (MappingOptimizeItem item : typeOptimizeItems) {
                properties.put(item.getFieldName(), item.getOptimize());
            }

            typeProperty.setProperties(properties);

            typeProperties.add(typeProperty);
        }

        return typeProperties;
    }

    private Result saveSpecialField(ConsoleTemplateSchemaDTO schemaDTO, String operator) throws AdminOperateException {
        if (CollectionUtils.isNotEmpty(schemaDTO.getFields())) {
            return saveSpecialFieldByField(schemaDTO, operator);
        } else {
            return saveSpecialFieldByJSON(schemaDTO, operator);
        }

    }

    private Result saveSpecialFieldByJSON(ConsoleTemplateSchemaDTO schemaDTO,
                                          String operator) throws AdminOperateException {
        List<AriusTypeProperty> typeProperties = schemaDTO.getTypeProperties();
        if (typeProperties.size() == 1 && (StringUtils.isBlank(typeProperties.get(0).getTypeName())
                                           || typeProperties.get(0).getTypeName().equals(DEFAULT_TYPE))) {
            // 就一个type，修改模板的id和routing字段
            IndexTemplateLogicDTO templateLogicDTO = new IndexTemplateLogicDTO();
            templateLogicDTO.setId(schemaDTO.getLogicId());
            templateLogicDTO.setIdField(typeProperties.get(0).getIdField());
            templateLogicDTO.setRoutingField(typeProperties.get(0).getRoutingField());
            templateLogicDTO.setDateField(typeProperties.get(0).getDateField());
            templateLogicDTO.setDateFieldFormat(typeProperties.get(0).getDateFieldFormat());
            Result editDateFieldResult = templateLogicService.editTemplate(templateLogicDTO, operator);
            if (editDateFieldResult.failed()) {
                return editDateFieldResult;
            }
        } else {

            // 修改type表
            List<IndexTemplateType> templateTypes = templateLogicService.getLogicTemplateTypes(schemaDTO.getLogicId());
            Map<String, IndexTemplateType> typeName2IndexTemplateTypeMap = ConvertUtil.list2Map(templateTypes,
                IndexTemplateType::getName);

            String dateField = typeProperties.get(0).getDateField();
            String dateFieldFormat = typeProperties.get(0).getDateFieldFormat();

            for (AriusTypeProperty typeProperty : typeProperties) {

                if (!Objects.equals(dateField, typeProperty.getDateField())) {
                    return Result.buildFail("多个type的分区字段必须一致");
                }

                if (typeName2IndexTemplateTypeMap.containsKey(typeProperty.getTypeName())) {
                    // 更新
                    IndexTemplateType indexTemplateType = typeName2IndexTemplateTypeMap.get(typeProperty.getTypeName());
                    TemplateTypePO param = new TemplateTypePO();
                    param.setId(indexTemplateType.getId());
                    param.setIdField(typeProperty.getIdField());
                    param.setRouting(typeProperty.getRoutingField());
                    if (1 != indexTemplateTypeDAO.update(param)) {
                        return Result.buildFail("保存特征字段失败");
                    } else {
                        LOGGER.info("method=saveSpecialFieldByJSON||msg=update db succ||typeId={}", param.getId());
                    }
                } else {
                    if (StringUtils.isNotBlank(typeProperty.getIdField())
                        || StringUtils.isNotBlank(typeProperty.getRoutingField())) {
                        return Result
                            .buildFail(
                                "平台升级es到高版本，高版本es索引仅支持单type，不再支持多个type；平台弱化了用户侧索引type的概念，索引多type需求用户可以通过创建多个索引来实现；\n"
                                       + "如需指定索引type的主键字段或者路由字段，请确认您的索引mapping中只有一个名为\"" + DEFAULT_TYPE + "\"的type；");
                    }
                }
            }

            // 修改模板的时间字段
            if (dateField != null || dateFieldFormat != null) {
                IndexTemplateLogicDTO templateLogicDTO = new IndexTemplateLogicDTO();
                templateLogicDTO.setId(schemaDTO.getLogicId());
                templateLogicDTO.setDateField(dateField);
                templateLogicDTO.setDateFieldFormat(dateFieldFormat);
                Result editDateFieldResult = templateLogicService.editTemplate(templateLogicDTO, operator);
                if (editDateFieldResult.failed()) {
                    return editDateFieldResult;
                }
            }
        }

        return Result.buildSucc();
    }

    private Result saveSpecialFieldByField(ConsoleTemplateSchemaDTO schemaDTO,
                                           String operator) throws AdminOperateException {
        SpecialField specialField = SpecialField.analyzeFromFields(schemaDTO.getFields(),
            schemaDTO.getRemoveFieldNames());
        IndexTemplateLogicDTO templateLogicDTO = new IndexTemplateLogicDTO();
        templateLogicDTO.setId(schemaDTO.getLogicId());
        templateLogicDTO.setDateField(specialField.getDateField());
        templateLogicDTO.setDateFieldFormat(specialField.getDateFieldFormat());
        templateLogicDTO.setIdField(specialField.getIdField());
        templateLogicDTO.setRoutingField(specialField.getRoutingField());
        return templateLogicService.editTemplate(templateLogicDTO, operator);
    }

    private boolean clusterIsHighVersion(Integer logicId) {
        IndexTemplateLogicWithPhyTemplates logicWithPhysical = this.templateLogicService
            .getLogicTemplateWithPhysicalsById(logicId);

        if (!logicWithPhysical.hasPhysicals()) {
            return false;
        }
        return esClusterPhyService.isHighVersionCluster(logicWithPhysical.getAnyOne().getCluster());
    }
}
