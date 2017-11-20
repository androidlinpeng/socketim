package com.websocketim.wechat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.websocketim.Constants;
import com.websocketim.R;

/**
 * Created by Administrator on 2017/11/11.
 */

public class ShareWechatTask extends AsyncTask<Object, Void, Void> {

    private Activity activity = null;

    private IWXAPI iwxapi = null;

    public ShareWechatTask(Activity activity) {
        this.activity = activity;
        this.iwxapi = WXAPIFactory.createWXAPI(activity.getApplicationContext(), Constants.APP_ID, false);
        this.iwxapi.registerApp(Constants.APP_ID);
    }

    @Override
    protected Void doInBackground(Object... params) {
        String content = (String) params[0];
        int scene = (Integer) params[1];

        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_launcher);
        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = "https://open.weixin.qq.com";
        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.mediaObject = webpageObject;
        mediaMessage.title = "包包圈";
        mediaMessage.description = content;
        mediaMessage.setThumbImage(bitmap);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = mediaMessage;
        req.scene = scene;
        req.transaction = String.valueOf(System.currentTimeMillis());
        if (!isCancelled()) {
            iwxapi.sendReq(req);
        }
        return null;
    }
}
