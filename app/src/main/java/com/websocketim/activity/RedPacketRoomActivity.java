package com.websocketim.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.websocketim.R;
import com.websocketim.fragment.DrawerFragment;
import com.websocketim.fragment.RedPacketRoomFragment;


public class RedPacketRoomActivity extends BaseActivity implements View.OnClickListener,
        RedPacketRoomFragment.OnRedFragmenClickListener{

    private DrawerLayout drawerLayout = null;
    private FragmentTransaction transaction;
    private RedPacketRoomFragment fragment;
    private DrawerFragment drawerFragment;
    private ImageView back;
    private TextView applyfor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(false);

        setContentView(R.layout.activity_red_packet_room);

        transaction = getSupportFragmentManager().beginTransaction();

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            this.fragment = new RedPacketRoomFragment();
            this.fragment.setArguments(bundle);
            transaction.replace(R.id.container,fragment).commitAllowingStateLoss();
        }

        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        this.drawerFragment = new DrawerFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.drawer_container, drawerFragment).commit();

        this.back = (ImageView) findViewById(R.id.back);
        this.applyfor = (TextView) findViewById(R.id.applyfor);
        this.back.setOnClickListener(this);
        this.applyfor.setOnClickListener(this);
        this.fragment.setOnRedFragmenClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.applyfor:
                openActivity(ApplyforNoticeActivity.class);
                break;
        }
    }

    @Override
    public void onOnRedFragmentClicked(Object obj) {
        drawerLayout.openDrawer(Gravity.LEFT);
        this.drawerFragment.newInstance("1","2");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if(this.drawerLayout.isDrawerOpen(Gravity.LEFT)){
                this.drawerLayout.closeDrawer(Gravity.LEFT);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
