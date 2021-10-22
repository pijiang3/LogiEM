package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

public class RetryExecutor {
    private static final ILog LOGGER     = LogFactory.getLog(RetryExecutor.class);

    /**
     * 最多的重试次数
     */
    private static int        RETRY_MAX  = 10;
    /**
     * 操作名字
     */
    private String            name       = "";
    /**
     * es操作内容
     */
    private Handler           handler;

    /**
     * 重试次数
     */
    private Integer           retryCount = 0;

    /**
     * es操作
     * Created by d06679 on 2017/8/24.
     */

    public interface Handler {
        boolean process() throws Throwable;

        default boolean needRetry(Throwable e) {
            return true;
        }

        default int retrySleepTime(int retryTimes) {return 0;}
    }

    public static RetryExecutor builder() {
        return new RetryExecutor();
    }

    public RetryExecutor name(String name) {
        this.name = name;
        return this;
    }

    public RetryExecutor handler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public RetryExecutor retryCount(Integer retryCount) {
        this.retryCount = (retryCount > RETRY_MAX) ? RETRY_MAX : retryCount;
        return this;
    }

    /**
     * 重试操作，要么handler执行成功有返回值,要么报异常
     * @throws Throwable 操作的异常
     */
    public boolean execute() throws Throwable {
        boolean succ = false;
        int tryCount = 0;
        do {
            try {
                int retrySleepTime = handler.retrySleepTime(tryCount);
                if(retrySleepTime > 0){
                    Thread.sleep(retrySleepTime);
                }

                succ = handler.process();
                if (succ) {
                    return succ;
                }
            } catch (Throwable e) {
                if (!handler.needRetry(e) || tryCount == retryCount) {
                    LOGGER.warn("class=RetryExecutor||method=execute||errMsg={}||handlerName={}||tryCount={}",
                        e.getMessage(), name, tryCount, e);
                    throw e;
                }
            }
        } while (tryCount++ < retryCount);

        return succ;
    }
}
