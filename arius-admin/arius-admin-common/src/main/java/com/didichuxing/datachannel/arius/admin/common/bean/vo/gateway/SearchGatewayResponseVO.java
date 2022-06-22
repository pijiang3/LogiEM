package com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author cjm
 */
@Deprecated
@Data
public class SearchGatewayResponseVO {

    /**
     * Gateway slow或error信息
     */
    @ApiModelProperty("Gateway slow或error信息")
    private List<GatewayJoinVO> records;

    /**
     * 查询命中记录数
     */
    @ApiModelProperty("查询命中记录数")
    private Long totalHits;
}