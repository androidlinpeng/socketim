package com.websocketim.wechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.websocketim.Constants;

/**
 * Created by Administrator on 2017/11/11.
 */

public class AppRegister extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final IWXAPI api = WXAPIFactory.createWXAPI(context, null);
        // 将该app注册到微信
        api.registerApp(Constants.APP_ID);
    }
}
