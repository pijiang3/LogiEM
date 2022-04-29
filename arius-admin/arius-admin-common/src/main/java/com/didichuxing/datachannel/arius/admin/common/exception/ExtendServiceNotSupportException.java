package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 *
 *
 * @author d06679
 * @date 2019/2/21
 */
public class ExtendServiceNotSupportException extends AriusRunTimeException {

    public ExtendServiceNotSupportException(String message) {
        super(message, ResultType.EXTEND_SERVICE_ERROR);
    }

}
