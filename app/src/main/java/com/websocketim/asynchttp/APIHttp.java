package com.websocketim.asynchttp;

import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by liang on 2017/2/28.
 */
public class APIHttp {

    private static final String TAG = "APIHttp";

    public APIHttp() {
    }

    public static ResultData get(String url){
        return Http.get(url);
    }

    public static ResultData put(String url, Map<String,String> values){
        return Http.put(url,values);
    }

    public static ResultData post(String url,Map values){
        RequestParams requesParams = new RequestParams();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Object vk:values.keySet()) {
            if (values.get(vk)instanceof List){
                for (String v:(List<String>)values.get(vk)) {
                    params.add(new BasicNameValuePair((String) vk,v));
                    requesParams.add((String) vk,v);
                }
            }else {
                params.add(new BasicNameValuePair((String)vk,(String)values.get(vk)));
                requesParams.add((String)vk,(String)values.get(vk));
            }
        }
        return Http.post(url,requesParams);
    }

    public static ResultData delete(String url){
        return Http.dalete(url);
    }

    public static ResultData uploadFile(String url,RequestParams patams){
        return Http.uploadFile(url,patams);
    }

}
