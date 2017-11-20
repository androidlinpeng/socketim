package com.websocketim.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.websocketim.R;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.ToastUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private ImageView back;
    private EditText ed_username;
    private EditText ed_password;
    private EditText ed_code;
    private TextView sendCode;
    private Button register;

    private String username;
    private String password;
    private String code;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.ed_username = (EditText) findViewById(R.id.ed_username);
        this.ed_password = (EditText) findViewById(R.id.ed_password);
        this.ed_code = (EditText) findViewById(R.id.ed_code);
        this.sendCode = (TextView) findViewById(R.id.bt_send_code);
        this.register = (Button) findViewById(R.id.bt_register);
        this.back = (ImageView) findViewById(R.id.back);
        this.back.setOnClickListener(this);
        this.sendCode.setOnClickListener(this);
        this.register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.bt_send_code:
                String time;
                username = ed_username.getText().toString().trim();
                if (!CommonUtil.isBlank(username)) {
                    sendCode.setEnabled(false);
                    time = CommonUtil.getCountDown(getApplication(), username);
                    if (!time.equals("60")) {
                        try {
                            Date date = new Date();
                            DateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                            String oldtime = CommonUtil.getDateToString(time);
                            Date codeTime = df.parse(oldtime);
                            long diff = date.getTime() - codeTime.getTime();
                            if (diff / (1000 * 60) < 1) {
                                time = String.valueOf(60 - diff / 1000);
                            } else {
                                time = "60";
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    Message msg = new Message();
                    msg.what = WHAT_START_TIME;
                    msg.obj = time;
                    handler.sendMessage(msg);
                } else {
                    ToastUtils.showShort(getApplication(), "用户名不能为空");
                }
                break;
            case R.id.bt_register:
                username = ed_username.getText().toString().trim();
                password = ed_password.getText().toString().trim();
                code = ed_code.getText().toString().trim();
                if (CommonUtil.isBlank(username)) {
                    ToastUtils.showShort(getApplication(),"用户名不能为空");
                    return;
                }
                if (CommonUtil.isBlank(password)) {
                    ToastUtils.showShort(getApplication(),"密码不能为空");
                    return;
                }
                if (CommonUtil.isBlank(code)) {
                    ToastUtils.showShort(getApplication(),"验证码不能为");
                    return;
                }
                ToastUtils.showShort(getApplication(),"注册成功");
                break;
        }

    }

    private final static int WHAT_START_TIME = 100;
    private final static int WHAT_UPDATE_TIME = 200;

    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_START_TIME:
                    final int time = Integer.parseInt((String) msg.obj);
                    if (time == 60) {
                        CommonUtil.setCountDown(getApplication(), username, String.valueOf(System.currentTimeMillis()));
                    }
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        int t = time;

                        @Override
                        public void run() {
                            if (t <= 0) {
                                timer.cancel();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendCode.setText("获取验证码");
                                        sendCode.setEnabled(true);
                                        CommonUtil.setCountDown(getApplication(), username, "60");
                                    }
                                });
                                return;
                            }
                            Message msg = new Message();
                            msg.what = WHAT_UPDATE_TIME;
                            msg.obj = t + " ";
                            handler.sendMessage(msg);
                            t--;
                        }
                    }, 0, 1000);
                    break;
                case WHAT_UPDATE_TIME:
                    sendCode.setText((String) msg.obj + "s");
                    break;
            }
        }
    };

}
