package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逻辑集群物理Rack信息
 * @author d06679
 * @date 2019/3/22
 */
@Data
public class ESClusterLogicRackInfo extends BaseEntity {

    /**
     * 主键
     */
    private Long id;

    /**
     * 逻辑集群ID
     */
    private Long logicClusterId;

    /**
     * Region Id
     */
    private Long regionId;

    /**
     * 物理集群名称
     */
    private String phyClusterName;

    /**
     * rack 当ownType是2时有值
     */
    private String rack;

}
