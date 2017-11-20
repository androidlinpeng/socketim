package com.websocketim.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.websocketim.Constants;
import com.websocketim.R;
import com.websocketim.model.UserInfo;
import com.websocketim.service.SocketService;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.MsgCache;
import com.websocketim.utils.PermissionUtils;

public class TestActivity extends BaseActivity {

    // activity是否可见
    public static boolean isShowing = false;

    public static Activity activity;

    public String master = "";
    private String avatorUrl ="http://img5.imgtn.bdimg.com/it/u=2919387663,1352833952&fm=27&gp=0.jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        activity = this;

        UserInfo userinfo = (UserInfo) MsgCache.get(getApplication()).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)){
            master = userinfo.getUsername();
//            avatorUrl = userinfo.getHead50();
        }

        //判断系统版本
        if (Build.VERSION.SDK_INT >= 23) {
            boolean isAllGranted = PermissionUtils.checkPermissionAllGranted(this);
            if (!isAllGranted) {
                PermissionUtils.requestPermissions(this, 1);
            }
        }

        findViewById(R.id.service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(TestActivity.this, SocketService.class));
            }
        });

        findViewById(R.id.chatFriend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TestActivity.this,IMActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.contactlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TestActivity.this,ContactListActivity.class));
            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TestActivity.this,RedPacketRoomActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        isShowing = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowing = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}