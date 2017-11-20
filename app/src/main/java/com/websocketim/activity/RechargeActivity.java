package com.websocketim.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.websocketim.R;


public class RechargeActivity extends BaseActivity implements View.OnClickListener {

    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);

        this.back = (ImageView) findViewById(R.id.back);

        this.back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                onBackPressed();
                break;
        }
    }
}
