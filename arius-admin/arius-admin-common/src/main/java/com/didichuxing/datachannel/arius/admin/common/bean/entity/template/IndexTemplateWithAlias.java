package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class IndexTemplateWithAlias extends IndexTemplate {

    private List<IndexTemplateAlias> aliases;

}