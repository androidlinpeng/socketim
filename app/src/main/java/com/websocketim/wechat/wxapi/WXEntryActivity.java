package com.websocketim.wechat.wxapi;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.websocketim.Constants;
import com.websocketim.activity.BaseActivity;
import com.websocketim.asynchttp.ResultData;
import com.websocketim.asynchttp.ResultManager;
import com.websocketim.utils.ToastUtils;
import com.websocketim.wechat.WeChatManager;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    private static final String TAG = "WXEntryActivity";

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        this.api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);
        this.api.registerApp(Constants.APP_ID);
        this.api.handleIntent(getIntent(), this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        this.api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp instanceof SendAuth.Resp){
            SendAuth.Resp resp = (SendAuth.Resp) baseResp;
            if (resp.errCode != 0){
                ToastUtils.showShort(getApplication(),"授权取消");
                finish();
            }else {
                WeChatManager.setCode(resp.code);
                new WXEntryTask().execute();
            }
        }else {
            finish();
        }
    }

    private class WXEntryTask extends AsyncTask<Object, Void, ResultData> {


        @Override
        protected ResultData doInBackground(Object... params) {
            return WeChatManager.fetchToken();
        }

        @Override
        protected void onPostExecute(ResultData data) {
            super.onPostExecute(data);
            if(!isFinishing()){
                if(ResultManager.isOk(data)){
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("third_wechat_get_token_success"));
                }else{
                    ToastUtils.showShort(getApplicationContext(), "获取授权信息失败");
                }
                finish();
            }
        }

    }

}
