package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.unit.ByteSizeValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterStatsResponse {
    /**
     * 索引个数
     */
    private long indexCount;
    /**
     * shard总个数
     */
    private long totalShard;
    /**
     * 文档个数
     */
    private long docsCount;
    /**
     * 总节点个数
     */
    private long totalNodes;
    /**
     * 磁盘总大小
     */
    private ByteSizeValue totalFs;
    /**
     * 剩余磁盘大小
     */
    private ByteSizeValue freeFs;
}
