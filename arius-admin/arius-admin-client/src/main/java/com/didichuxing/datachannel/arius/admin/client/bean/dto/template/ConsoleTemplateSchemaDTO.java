package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.client.mapping.Field;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019-06-13
 */
@Data
@ApiModel(description = "索引schema信息")
public class ConsoleTemplateSchemaDTO extends BaseDTO {

    @ApiModelProperty("索引ID")
    private Integer                 logicId;

    /**
     * mapping信息 手动设置的
     */
    @ApiModelProperty("索引schema列表信息")
    private List<Field>             fields;

    /**
     * 需要删除的字段
     */
    @ApiModelProperty("索引mapping删除字段列表")
    private Set<String>             removeFieldNames;

    /**
     * type
     */
    @ApiModelProperty("索引schemajson信息")
    private List<AriusTypeProperty> typeProperties;
}
