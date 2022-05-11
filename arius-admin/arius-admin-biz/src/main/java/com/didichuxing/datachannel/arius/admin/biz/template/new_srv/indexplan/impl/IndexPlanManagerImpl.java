package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.indexplan.IndexPlanManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chengxiang, jiamin
 * @date 2022/5/11
 */
@Service
public class IndexPlanManagerImpl extends BaseTemplateSrvImpl implements IndexPlanManager {

    @Autowired
    private ESIndexService esIndexService;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.INDEX_PLAN;
    }

    @Override
    public Result<Void> indexRollover(Integer logicTemplateId) {
        LOGGER.info("class=IndexPlanManagerImpl||method=indexRollover||logicTemplateId={}||msg=start indexRollover", logicTemplateId);
        if (!isTemplateSrvOpen(logicTemplateId)) {
            return Result.buildFail("指定索引模板未开启rollover能力");
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> adjustShardCountByPhyClusterName(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    public void initShardRoutingAndAdjustShard(IndexTemplatePhyDTO param) {

    }



}
