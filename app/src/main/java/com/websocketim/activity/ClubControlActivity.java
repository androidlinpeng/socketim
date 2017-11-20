package com.websocketim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.websocketim.Constants;
import com.websocketim.R;
import com.websocketim.fragment.ClubChatFragment;
import com.websocketim.fragment.ClubRoomFragment;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.MsgCache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.websocketim.utils.CommonUtil.dip2px;

public class ClubControlActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ClubControlActivity";
    
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView back;

    private ClubHallPagerAdapter mAdpter;
    private List<Fragment> list = new ArrayList<Fragment>();

    private ClubChatFragment chatfragment;
    private ClubRoomFragment roomfragment;

    private String master = "";
    private String avator = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(false);

        setContentView(R.layout.activity_club);

        UserInfo userinfo = (UserInfo) MsgCache.get(ClubControlActivity.this).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
            avator = userinfo.getHead50();
        }

        back = (ImageView) findViewById(R.id.back);
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        chatfragment = new ClubChatFragment();
        roomfragment = new ClubRoomFragment();
        list.add(chatfragment);
        list.add(roomfragment);
        setUpIndicatorWidth(tabLayout,50,50);
        tabLayout.addTab(tabLayout.newTab().setText("聊天"));
        tabLayout.addTab(tabLayout.newTab().setText("房间"));
        mAdpter = new ClubHallPagerAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(mAdpter);
        viewPager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(viewPager);

        back.setOnClickListener(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        chatfragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
        }
    }

    private class ClubHallPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> mlist = new ArrayList<Fragment>();

        public ClubHallPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            mlist = list;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = mlist.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("master", master);
            bundle.putString("avator", avator);
            bundle.putString("chat_type", Constants.FRAGMENT_GROUP);
            bundle.putString("chat_friend", "157");
            bundle.putString("chat_friendNick", "157");
            if (null != bundle) {
                Log.d(TAG, "getItem: ");
                fragment.setArguments(bundle);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            if (position == 0){
                title = "聊天";
            }else {
                title = "房间";
            }
            return title;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(state, loader);
        }
    }


    private void setUpIndicatorWidth(final TabLayout tabLayout, final int marginLeft, final int marginRight) {
        //了解源码得知 线的宽度是根据 tabView的宽度来设置的
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //拿到tabLayout的mTabStrip属性
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);

                    int leftdp = dip2px(tabLayout.getContext(), marginLeft);
                    int rightdp = dip2px(tabLayout.getContext(), marginRight);

                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabView = mTabStrip.getChildAt(i);
                        //拿到tabView的mTextView属性  tab的字数不固定一定用反射取mTextView
                        Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                        mTextViewField.setAccessible(true);
                        TextView mTextView = (TextView) mTextViewField.get(tabView);
                        tabView.setPadding(0, 0, 0, 0);
                        //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                        int width = 0;
                        width = mTextView.getWidth();
                        if (width == 0) {
                            mTextView.measure(0, 0);
                            width = mTextView.getMeasuredWidth();
                        }
                        //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                        params.width = width;
                        params.leftMargin = leftdp;
                        params.rightMargin = rightdp;
                        tabView.setLayoutParams(params);
                        tabView.invalidate();
                    }

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
