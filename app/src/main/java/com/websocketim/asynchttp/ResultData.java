package com.websocketim.asynchttp;

/**
 * Created by liang on 2017/2/28.
 */
public class ResultData {
    private final static String TAG="ResultData";

    private int code;
    private String message;
    private Object data;
    private int httpCode;

    public ResultData(){
        this.code= ResultManager.OK;
        this.message="";
    }

    public ResultData(int code,String message,Object data){
        this.code=code;
        this.message=message;
        this.data=data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

}
