package com.websocketim.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.websocketim.Constants;
import com.websocketim.view.Displayer;
import com.websocketim.R;
import com.websocketim.manager.IMChatManager;
import com.websocketim.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class UserInfoPreviewActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "UserInfoPreviewActivity";
    private DisplayImageOptions options;

    private ImageView back;
    private TextView mTv_username;
    private TextView mTv_nickname;
    private ImageView iv_avatar;
    private ListView listView;
    private Button sendMsg;

    private UserInfoAdaper adaper;
    private List<String> mList = new ArrayList<String>();

    private String master = "";
    private String username = "";
    private String nickname = "";
    private String avatar = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_preview);

        int ic_head_defaultId = R.drawable.ic_head_default;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(ic_head_defaultId)
                .showImageForEmptyUri(ic_head_defaultId)
                .showImageOnFail(ic_head_defaultId)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new Displayer(0))
                .build();

        Bundle bundle = getIntent().getExtras();
        if (null != bundle){
            master = bundle.getString("master");
            avatar = bundle.getString("avatar");
            username = bundle.getString("username");
            nickname = bundle.getString("nickname");
            Log.d(TAG, "onCreate: "+nickname);
        }
        if (CommonUtil.isBlank(username)){
            finish();
            return;
        }

        this.back = (ImageView) findViewById(R.id.back);
        this.iv_avatar = (ImageView) findViewById(R.id.avatar);
        this.listView = (ListView) findViewById(R.id.listView);
        this.mTv_username = (TextView) findViewById(R.id.mTv_username);
        this.mTv_nickname = (TextView) findViewById(R.id.mTv_nickname);
        this.sendMsg = (Button) findViewById(R.id.sendMsg);

        this.back.setOnClickListener(this);
        this.sendMsg.setOnClickListener(this);

        this.mTv_username.setText(nickname);
        this.mTv_nickname.setText(username);
//        ImageLoader.getInstance().displayImage(avatarUrl, this.avatar, options);

        this.adaper = new UserInfoAdaper(mList);
        this.listView.setAdapter(adaper);
        for (int i = 0; i < 4; i++) {
            mList.add(" " + i);
        }
        this.adaper.setDate(mList);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int backId = R.id.back;
        int sendMsgId = R.id.sendMsg;
        if (id == backId){
            onBackPressed();
        }else if (id == sendMsgId) {
            Log.d(TAG, "onClick: "+master+" "+username+" "+nickname);
            IMChatManager.startChat(UserInfoPreviewActivity.this, master, avatar,Constants.FRAGMENT_FRIEND, username, nickname);
            finish();
        }
    }

    public class UserInfoAdaper extends BaseAdapter {

        private List<String> mList;

        public UserInfoAdaper(List<String> mList) {
            this.mList = mList;
        }

        public void setDate(List<String> mList) {
            this.mList = mList;
            notifyDataSetChanged();

        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                int layoutId = R.layout.view_user_info_itme;
                convertView = getLayoutInflater().inflate(layoutId, null);
                viewHolder = new ViewHolder();
                int titleId = R.id.title;
                int contentId = R.id.content;
                viewHolder.title = convertView.findViewById(titleId);
                viewHolder.content = convertView.findViewById(contentId);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.title.setText("title");
            viewHolder.content.setText("content");

            return convertView;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView content;
    }
}

