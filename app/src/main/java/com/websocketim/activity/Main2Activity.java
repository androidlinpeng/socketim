package com.websocketim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.websocketim.Constants;
import com.websocketim.R;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.MsgCache;

public class Main2Activity extends AppCompatActivity {

    static final int ACTIVITY_REQUEST_CODE_A = 100;
    private String master;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

//        //系统广播监听服务
//        MainReceiver receiver = new MainReceiver();
//        IntentFilter filter=new IntentFilter();
//        filter.addAction(Intent.ACTION_TIME_TICK);
//        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
//        filter.addAction(CONNECTIVITY_ACTION);
//        registerReceiver(receiver,filter);

        findViewById(R.id.textView1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername("4827");
                userInfo.setHead50("http://img3.imgtn.bdimg.com/it/u=1492752078,3094009867&fm=27&gp=0.jpg");
                MsgCache.get(getApplication()).put(Constants.USER_INFO, userInfo);
                master = "4827";
                startActivity();
            }
        });

        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername("4834");
                userInfo.setHead50("http://img3.imgtn.bdimg.com/it/u=1492752078,3094009867&fm=27&gp=0.jpg");
                MsgCache.get(getApplication()).put(Constants.USER_INFO, userInfo);
                master = "4834";
                startActivity();
            }
        });

        findViewById(R.id.textView3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername("4872");
                userInfo.setHead50("http://img3.imgtn.bdimg.com/it/u=1492752078,3094009867&fm=27&gp=0.jpg");
                MsgCache.get(getApplication()).put(Constants.USER_INFO, userInfo);
                master = "4872";
                startActivity();
            }
        });
    }

    public void startActivity(){
        Intent intent = new Intent(Main2Activity.this, TestActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
    }
}
