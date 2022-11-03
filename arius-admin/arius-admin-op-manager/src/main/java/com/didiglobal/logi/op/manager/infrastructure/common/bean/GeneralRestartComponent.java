package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-09-01 11:04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralRestartComponent extends GeneralBaseOperationComponent {

    private Integer componentId;

    public Result<Void> checkRestartParam() {
        Result result = super.checkParam();

        if (result.failed()) {
            return result;
        }

        if (null == componentId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "组件id缺失");
        }

        return Result.success();
    }
}
