package com.websocketim.asynchttp;

import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.util.Map;

/**
 * Created by liang on 2017/2/28.
 */
public class Http {

    private static final String TAG = "Http";

    private static SyncHttpClient httpClient = new SyncHttpClient();

    private static SyncHttpClient uploadFileHttpClient = new SyncHttpClient();

    static {
        httpClient.setTimeout(15 * 1000);
        uploadFileHttpClient.setTimeout(60 * 1000);
    }

    public static SyncHttpClient getSyncHttpClient() {
        return httpClient;
    }

    public static SyncHttpClient getuploadFileHttpClient() {
        return uploadFileHttpClient;
    }

    public static ResultData get(String url) {

        ResultData data = new ResultData();
        httpClient.get(url, new HttpResponseHandler(data, url));

        return data;
    }

    public static ResultData post(String url, RequestParams params) {

        ResultData data = new ResultData();
        httpClient.post(url, params, new HttpResponseHandler(data, url));

        return data;
    }

    public static ResultData put(String url, Map<String, String> values) {

        ResultData data = new ResultData();
        RequestParams params = new RequestParams(values);
        httpClient.put(url, params, new HttpResponseHandler(data, url));

        return data;
    }

    public static ResultData dalete(String url) {

        ResultData data = new ResultData();
        httpClient.delete(url, new HttpResponseHandler(data, url));

        return data;
    }

    public static ResultData uploadFile(String url, RequestParams params) {

        ResultData data = new ResultData();
        uploadFileHttpClient.post(url, params, new HttpResponseHandler(data, url));

        return data;
    }
}
