package com.websocketim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.websocketim.R;
import com.websocketim.model.ChatMessage;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.ToastUtils;


public class SendRedPacketActivity extends BaseActivity implements View.OnClickListener{

    private EditText redname;
    private EditText redpoint;
    private EditText rednumber;
    private EditText redbomb;
    private ImageView back;
    private Button sendRed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_red_packet);

        this.redname = (EditText) findViewById(R.id.redname);
        this.redpoint = (EditText) findViewById(R.id.redpoint);
        this.rednumber = (EditText) findViewById(R.id.rednumber);
        this.redbomb = (EditText) findViewById(R.id.redbomb);
        this.back = (ImageView) findViewById(R.id.back);
        this.sendRed = (Button) findViewById(R.id.sendRed);
        this.back.setOnClickListener(this);
        this.sendRed.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.sendRed:
                sendText();
                break;
        }
    }

    private void sendText() {
        // 构造msg
        String name = redname.getText().toString().trim();
        String point = redpoint.getText().toString().trim();
        String number = rednumber.getText().toString().trim();
        String bomb = redbomb.getText().toString().trim();
        // 空消息不发送
        if (CommonUtil.isBlank(name)||CommonUtil.isBlank(point)||CommonUtil.isBlank(number)||CommonUtil.isBlank(bomb)) {
            ToastUtils.showShort(getApplication(),"内容不能为空");
            return;
        }
        ChatMessage cm = new ChatMessage();
        cm.setContent(name);
        cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_TXT);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("redinfor",cm);
        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);
        finish();
    }

}
