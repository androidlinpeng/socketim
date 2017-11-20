package com.websocketim.wechat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.websocketim.Constants;
import com.websocketim.MyApplication;
import com.websocketim.asynchttp.Http;
import com.websocketim.asynchttp.ResultData;
import com.websocketim.asynchttp.ResultManager;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.ToastUtils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/11/11.
 */

public class WeChatManager {

    private static final String SP_WECHAT_INFO = "third_wechat_info";
    private static final String SP_WECHAT_CODE = "third_wechat_code";
    private static final String SP_WECHAT_OPENID = "third_wechat_openid";
    private static final String SP_WECHAT_TOKEN = "third_wechat_token";

    public static final String URL_GET_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    public static final String URL_GET_USER_INFO = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

    public static void startAuth(Context cxt) {
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(cxt, Constants.APP_ID, false);
        iwxapi.registerApp(Constants.APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        iwxapi.sendReq(req);
    }

    public static ResultData fetchToken() {
        String code = getCode();
        String url = String.format(URL_GET_TOKEN, Constants.APP_ID, Constants.APP_SECRET, code);
        ResultData data = Http.get(url);
        if (ResultManager.isOk(data)) {
            try {
                JSONObject json = new JSONObject((String) data.getData());
                String errorCode = json.optString("errcode");
                if (!CommonUtil.isBlank(errorCode)) {
                    data = ResultManager.createFailData("授权失败");
                } else {
                    setToken(json.optString("access_token"));
                    setOpenid(json.optString("openid"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static ResultData fetchUserInfo() {
        String url = String.format(URL_GET_USER_INFO, getToken(), getOpenid());
        ResultData data = Http.get(url);
        if (ResultManager.isOk(data)) {
            try {
                JSONObject json = new JSONObject((String) data.getData());
                String errorCode = json.optString("errcode");
                if (!CommonUtil.isBlank(errorCode)) {
                    data = ResultManager.createFailData("授权失败");
                } else {
                    String nickname = json.optString("nickname");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static ResultData SubmitwechatUserInfo() {
        String openId = getOpenid();
        ResultData data = null;
        if (!CommonUtil.isBlank(openId)) {
            try {
                data = fetchUserInfo();
                if (ResultManager.isOk(data)) {
                    String wechatUserJson = (String) data.getData();
                    JSONObject userJson = new JSONObject(wechatUserJson);
                    String fName = userJson.optString("nickname");
                    String headimgurl = userJson.optString("headimgurl");
                    HashMap<String, String> values = new HashMap<String, String>();
                    values.put("openid", openId);
                    values.put("nickname", fName);
                    values.put("headimgurl", headimgurl);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static void send(Activity activity,int scene){
        ShareWechatTask asyncTask = new ShareWechatTask(activity);
        IWXAPI iwxapi = WXAPIFactory.createWXAPI(activity, Constants.APP_ID, false);
        iwxapi.registerApp(Constants.APP_ID);
        if (iwxapi.isWXAppInstalled()) {
            if (scene == SendMessageToWX.Req.WXSceneTimeline) {
                if (iwxapi.getWXAppSupportAPI() >= 0x21020001) {
                    asyncTask.execute("包包圈...抢红包啦", scene);
                } else {
                    ToastUtils.showShort(activity, "您的微信版本不支持朋友圈");
                }
            } else {
                asyncTask.execute("包包圈...抢红包啦", scene);
            }

        } else {
            ToastUtils.showShort(activity, "未安装微信客户端");
        }
    }

    public static void setCode(String code) {
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO, Activity.MODE_PRIVATE);
        sp.edit().putString(SP_WECHAT_CODE, code).apply();
    }

    public static String getCode() {
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO, Activity.MODE_PRIVATE);
        return sp.getString(SP_WECHAT_CODE, "");
    }

    public static void setToken(String token) {
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        sp.edit().putString(SP_WECHAT_TOKEN, token).apply();
    }

    public static String getToken() {
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        return sp.getString(SP_WECHAT_TOKEN, "");
    }

    public static void setOpenid(String openid) {
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        sp.edit().putString(SP_WECHAT_OPENID, openid).apply();
    }

    public static String getOpenid() {
        SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(SP_WECHAT_INFO,
                Activity.MODE_PRIVATE);
        return sp.getString(SP_WECHAT_OPENID, "");
    }

}
