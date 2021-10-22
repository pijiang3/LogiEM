package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.task.TaskConcurrentConstants;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.task.BaseConcurrentClusterTask;

/**
 * 删除过期索引任务
 * @author d06679
 * @date 2019/4/8
 */
@Component
public class CopyIndexMapping2TemplateTask extends BaseConcurrentClusterTask {

    /**
     * 获取任务名称
     *
     * @return 任务名称
     */
    @Override
    public String getTaskName() {
        return "CopyMapping";
    }

    /**
     * 并发度
     *
     * @return
     */
    @Override
    public int current() {
        return TaskConcurrentConstants.COPY_INDEX_MAPPING2_TEMPLATE_TASK_CONCURRENT;
    }

    /**
     * 同步处理指定集群
     *
     * @param cluster 集群名字
     */
    @Override
    public boolean executeByCluster(String cluster) throws AdminOperateException {
        return clusterPhyManager.copyMapping(cluster, 5);
    }

}
