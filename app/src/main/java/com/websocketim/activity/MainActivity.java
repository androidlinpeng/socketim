package com.websocketim.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.websocketim.Constants;
import com.websocketim.DBHelper;
import com.websocketim.MainReceiver;
import com.websocketim.R;
import com.websocketim.fragment.CenterFragment;
import com.websocketim.fragment.CombatGainsFragment;
import com.websocketim.fragment.DiscoverFragment;
import com.websocketim.fragment.MyFragment;
import com.websocketim.fragment.NewsFragment;
import com.websocketim.model.ChatMessage;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.MsgCache;
import com.websocketim.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private FrameLayout customPage = null;

    private FragmentManager manager;

    private FragmentTransaction transaction;

    private DiscoverFragment discover = new DiscoverFragment();

    private NewsFragment news = new NewsFragment();

    private CenterFragment center = new CenterFragment();

    private CombatGainsFragment combatGains = new CombatGainsFragment();

    private MyFragment mine = new MyFragment();

    private View currentButton;

    private ImageButton mDiscover, mNews, mCenter, mCombat, mMine = null;
    private TextView title, mTvDiscover, mTvNews, mTvCenter, mTvCombat, mTvMine = null;
    private TextView mTvunread;

    private MainReceiver receiver;
    private NewMsgReceiver newMsgReceiver;

    private String master = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(false);

        setContentView(R.layout.activity_main);

        UserInfo userinfo = (UserInfo) MsgCache.get(MainActivity.this).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
        }

        initView();

        //系统广播监听服务
        this.receiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_REBOOT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);

        registerReceiver(this.receiver, filter);

        this.newMsgReceiver = new NewMsgReceiver();
        registerReceiver(this.newMsgReceiver, new IntentFilter(Constants.CHAT_NEW_MESSAGE));

    }

    private void initView() {
//        mDiscover = (ImageButton) findViewById(R.id.buttom_discover);
//        mNews = (ImageButton) findViewById(R.id.buttom_news);
//        mCenter = (ImageButton) findViewById(R.id.buttom_center);
//        mCombat = (ImageButton) findViewById(R.id.buttom_combat_gains);
//        mMine = (ImageButton) findViewById(R.id.buttom_my);
//
//        mDiscover.setOnClickListener(this);
//        mNews.setOnClickListener(this);
//        mCenter.setOnClickListener(this);
//        mCombat.setOnClickListener(this);
//        mMine.setOnClickListener(this);

        customPage = (FrameLayout) findViewById(R.id.content_frame);
        manager = getSupportFragmentManager();

        mTvDiscover = (TextView) findViewById(R.id.tv_discover);
        mTvNews = (TextView) findViewById(R.id.tv_news);
        mTvCenter = (TextView) findViewById(R.id.tv_center);
        mTvCombat = (TextView) findViewById(R.id.tv_combat_gains);
        mTvMine = (TextView) findViewById(R.id.tv_mine);


        mTvDiscover.setOnClickListener(this);
        mTvNews.setOnClickListener(this);
        mTvCenter.setOnClickListener(this);
        mTvCombat.setOnClickListener(this);
        mTvMine.setOnClickListener(this);

        title = (TextView) findViewById(R.id.title);
        mTvunread = (TextView) findViewById(R.id.unread_tip);

        mTvCenter.performClick();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_discover:
                setButton(view);
                replaceContentFrameFragment(discover);
                textContainer(0);
                title.setText("发现");
                mTvDiscover.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_selected));
                mTvNews.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvCombat.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvMine.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                break;
            case R.id.tv_news:
                setButton(view);
                replaceContentFrameFragment(news);
                title.setText("消息");
                mTvDiscover.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvNews.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_selected));
                mTvCombat.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvMine.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                break;
            case R.id.tv_center:
                setButton(view);
                replaceContentFrameFragment(center);
                title.setText("俱乐部");
                mTvDiscover.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvNews.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvCombat.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvMine.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                break;
            case R.id.tv_combat_gains:
                setButton(view);
                replaceContentFrameFragment(combatGains);
                title.setText("战绩");
                mTvDiscover.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvNews.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvCombat.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_selected));
                mTvMine.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                break;
            case R.id.tv_mine:
                setButton(view);
                replaceContentFrameFragment(mine);
                title.setText("我的");
                mTvDiscover.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvNews.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvCombat.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_normal));
                mTvMine.setTextColor(getResources().getColor(R.color.socket_tab_text_coloer_selected));
                break;
            default:
                break;
        }
    }

    private void textContainer(int i) {

    }

    private void replaceContentFrameFragment(Fragment fragment) {
        if (null != fragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    private void setButton(View v) {
        if (currentButton != null && currentButton.getId() != v.getId()) {
            currentButton.setEnabled(true);
        }
        v.setEnabled(false);
        currentButton = v;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        new GetRecentListTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (null != receiver) {
            unregisterReceiver(receiver);
        }
        if (null != newMsgReceiver) {
            unregisterReceiver(newMsgReceiver);
        }
    }

    private int keyBackClickCount;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown: 1 " + keyBackClickCount);
            switch (keyBackClickCount++) {
                case 0:
                    Log.d(TAG, "onKeyDown: 2 " + keyBackClickCount);
                    ToastUtils.showShort(getApplication(), "再按一次退出");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            keyBackClickCount = 0;
                        }
                    }, 3000);
                    break;
                case 1:
                    Log.d(TAG, "onKeyDown: 3 " + keyBackClickCount);
                    finish();
                    break;
                default:
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 新消息广播接收器
    private class NewMsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent && intent.getAction().equals(Constants.CHAT_NEW_MESSAGE)) {
                new GetRecentListTask().execute();
            }
        }
    }

    private class GetRecentListTask extends AsyncTask<Void, Void, List<ChatMessage>> {

        @Override
        protected List<ChatMessage> doInBackground(Void... params) {
            Context cxt = getApplicationContext();
            DBHelper helper = new DBHelper(cxt);
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cur = db.rawQuery("SELECT * FROM chat_recent WHERE username='" + master + "' ORDER BY msg_time DESC", null);
            List<ChatMessage> cms = new ArrayList<ChatMessage>();
            while (cur.moveToNext()) {
                ChatMessage cm = new ChatMessage();
                cm.setFromuser(cur.getString(cur.getColumnIndex("msg_from")));
                cm.setFromusernick(cur.getString(cur.getColumnIndex("msg_fromnick")));
                cm.setTouser(cur.getString(cur.getColumnIndex("msg_to")));
                cm.setTousernick(cur.getString(cur.getColumnIndex("msg_tonick")));
                cm.setContent(cur.getString(cur.getColumnIndex("msg_content")));
                cm.setTime(cur.getString(cur.getColumnIndex("msg_time")));
                cm.setClientId(cur.getString(cur.getColumnIndex("msg_clientId")));
                cm.setAvatar(cur.getString(cur.getColumnIndex("msg_avatar")));
                cm.setNickname(cur.getString(cur.getColumnIndex("msg_nickname")));
                cm.setUsername(cur.getString(cur.getColumnIndex("msg_username")));
                cm.setType(cur.getString(cur.getColumnIndex("msg_type")));
                cm.setUnreadCount(cur.getInt(cur.getColumnIndex("msg_unread_count")));
                cm.setUrl(cur.getString(cur.getColumnIndex("msg_media_url")));
                cm.setContentType(cur.getString(cur.getColumnIndex("msg_content_type")));
                cm.setExtra(cur.getString(cur.getColumnIndex("msg_media_extra")));
                cms.add(cm);
            }
            Log.d("", "doInBackground: " + cms.size());
            cur.close();
            db.close();
            return cms;
        }

        @Override
        protected void onPostExecute(List<ChatMessage> result) {
            int unread = 0;
            for (ChatMessage msg : result) {
                unread += msg.getUnreadCount();
            }
            if (unread > 0) {
                mTvunread.setVisibility(View.VISIBLE);
                mTvunread.setText("" + unread);
            } else {
                mTvunread.setVisibility(View.GONE);
                mTvunread.setText("");
            }
        }
    }

}
