package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleTemplateAliasSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

public interface TemplateLogicAliasService {

    /**
     * 获取所有的别名
     *
     * @param logicId logicId
     * @return list
     */
    List<String> getAliasesById(Integer logicId);

    /**
     * 从缓存中获取模板所有的别名
     * @param logicId
     * @return
     */
    List<String> getAliasesByIdFromCache(Integer logicId);

    /**
     * 获取别名
     *
     * @return list
     */
    List<IndexTemplateAlias> listAlias();

    /**
     * 增加一个索引的别名
     *
     * @param indexTemplateAlias
     * @return
     */
    Result<Boolean> addAlias(IndexTemplateAliasDTO indexTemplateAlias);

    /**
     * 删除一个索引的别名
     *
     * @param indexTemplateAlias
     * @return
     */
    Result<Boolean> delAlias(IndexTemplateAliasDTO indexTemplateAlias);

    /**
     * 获取平台所有索引别名
     *
     * @return
     */
    Map<Integer, List<String>> listAliasMap();

    /**
     * 从缓存中获取平台所有索引别名
     * @return
     */
    Map<Integer, List<String>> listAliasMapWithCache();

    /**
     * 别名切换功能
     * @param aliasSwitchDTO
     * @return
     * @throws ESOperateException
     */
    Result aliasSwitch(ConsoleTemplateAliasSwitchDTO aliasSwitchDTO) throws ESOperateException;
}
