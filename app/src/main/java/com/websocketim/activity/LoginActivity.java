package com.websocketim.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.websocketim.Constants;
import com.websocketim.R;
import com.websocketim.model.UserInfo;
import com.websocketim.service.SocketService;
import com.websocketim.utils.MsgCache;
import com.websocketim.utils.PermissionUtils;
import com.websocketim.wechat.WeChatManager;


public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private EditText username = null;
    private EditText password = null;
    private Button bt_login;
    private TextView tv_regester;
    private ImageView iv_weixin;
    private ImageView iv_phone;

    private boolean personalCenter = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(false);

        setContentView(R.layout.activity_login);

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            personalCenter = bundle.getBoolean("personalCenter");
        }

        //判断系统版本
        if (Build.VERSION.SDK_INT >= 23) {
            boolean isAllGranted = PermissionUtils.checkPermissionAllGranted(this);
            if (!isAllGranted) {
                PermissionUtils.requestPermissions(this, 1);
            }
        }

        username = (EditText) findViewById(R.id.ed_login_username);
        password = (EditText) findViewById(R.id.ed_login_password);
        bt_login = (Button) findViewById(R.id.bt_login);
        tv_regester = (TextView) findViewById(R.id.tv_goto_regester);
        iv_weixin = (ImageView) findViewById(R.id.iv_weixin);
        iv_phone = (ImageView) findViewById(R.id.iv_phone);

        bt_login.setOnClickListener(this);
        tv_regester.setOnClickListener(this);
        iv_weixin.setOnClickListener(this);
        iv_phone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_login:
//                String master = null;
//                UserInfo userinfo = (UserInfo) MsgCache.get(LoginActivity.this).getAsObject(Constants.USER_INFO);
//                if (!CommonUtil.isBlank(userinfo)) {
//                    master = userinfo.getMaster();
//                }
                String pwd = "pwd";
                String user = this.username.getText().toString().trim();
//                String pwd = this.password.getText().toString().trim();
//                if (!CommonUtil.isBlank(master)){
//                    user = master;
//                }else if (CommonUtil.isBlank(user) || CommonUtil.isBlank(pwd)) {
//                    ToastUtils.showShort(this, "用户名不能为空");
//                    return;
//                }
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(user);
                userInfo.setNickname("我");
                userInfo.setHead50("http://img2.imgtn.bdimg.com/it/u=3200373527,2802703123&fm=27&gp=0.jpg");
                MsgCache.get(getApplication()).put(Constants.USER_INFO, userInfo);
                startService(new Intent(LoginActivity.this, SocketService.class));
                if (!personalCenter) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }else {
                    setResult(RESULT_OK);
                }
                finish();
                break;
            case R.id.tv_goto_regester:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
            case R.id.iv_weixin:
                WeChatManager.startAuth(getApplicationContext());
                break;
            case R.id.iv_phone:
                WeChatManager.send(LoginActivity.this, SendMessageToWX.Req.WXSceneSession);
                break;
        }
    }
}
