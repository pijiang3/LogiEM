package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.AppIdTemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.app.AppIdTemplateAccessESDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ESClusterStaticsService {

    @Autowired
    private IndexTemplateInfoService indexTemplateInfoService;

    @Autowired
    private AppIdTemplateAccessESDAO   accessCountEsDao;

    public List<Integer> getLogicClusterAccessInfo(Long logicClusterId, int days){
        List<Integer> appids = new ArrayList<>();

        List<IndexTemplateInfo> templateLogics = indexTemplateInfoService.getLogicClusterTemplates(logicClusterId);

        for(IndexTemplateInfo indexTemplate : templateLogics){
            List<AppIdTemplateAccessCountPO> accessCountPos = accessCountEsDao.getAccessAppidsInfoByTemplateId(indexTemplate.getId(), days);
            if(CollectionUtils.isNotEmpty(accessCountPos)){
                accessCountPos.forEach(a -> appids.add(a.getAppId()));
            }
        }

        return appids;
    }
}
