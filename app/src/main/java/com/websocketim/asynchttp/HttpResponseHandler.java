package com.websocketim.asynchttp;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.websocketim.MyApplication;
import com.websocketim.R;
import com.websocketim.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by liang on 2017/2/28.
 */
public class HttpResponseHandler extends AsyncHttpResponseHandler {

    private static final String TAG = "HttpResponseHandler";

    private ResultData data;

    private String url = "";

    public HttpResponseHandler(ResultData data, String url) {
        this.data = data;
        this.url = url;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        if (null!=responseBody){
            this.data.setData(new String(responseBody));
        }
        this.data.setHttpCode(statusCode);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        String message = MyApplication.getInstance().getString(R.string.str_network_not_good);
        this.data.setMessage(message);
        if (null!=responseBody){
            this.data.setData(new String(responseBody));
            try {
                JSONObject json=new JSONObject(new String(responseBody));
                JSONArray jsonKeys=json.names();
                if(null!=jsonKeys){
                    StringBuilder sb=new StringBuilder();
                    for(int i=0;i<jsonKeys.length();i++){
                        String value=json.optString(jsonKeys.optString(i));
                        value=value.replace("[\"", "");
                        value=value.replace("\"]", "");
                        sb.append(value+"\n");
                    }
                    sb.deleteCharAt(sb.length()-1);
                    sb.deleteCharAt(sb.length()-1);
                    if("。".equals(sb.charAt(sb.length()-1))){
                        sb.deleteCharAt(sb.length()-1);
                    }
                    this.data.setMessage(sb.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.data.setCode(ResultManager.ERROR);
        this.data.setHttpCode(statusCode);
    }

    @Override
    public void onFinish() {
        LogUtil.i(TAG, "http_url: " + this.url);
        LogUtil.i(TAG, "http_code: "+this.data.getHttpCode());
        LogUtil.i(TAG, "content: "+this.data.getData());
    }


}
