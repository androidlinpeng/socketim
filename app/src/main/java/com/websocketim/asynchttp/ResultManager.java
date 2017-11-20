package com.websocketim.asynchttp;

/**
 * Created by liang on 2017/2/28.
 */
public class ResultManager {

    final public static int ERROR = 100;

    final public static int OK = 200;

    public static boolean isOk(ResultData data) {
        if (data == null)
            return false;
        return data.getCode() == OK;
    }

    public static ResultData createSuccessData(Object obj) {
        ResultData data = new ResultData();
        data.setCode(OK);
        data.setData(obj);
        return data;
    }

    public static ResultData createFailData(String message) {
        ResultData data = new ResultData();
        data.setCode(ERROR);
        data.setMessage(message);
        return data;
    }
}
