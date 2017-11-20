package com.websocketim.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.websocketim.Constants;
import com.websocketim.DBHelper;
import com.websocketim.R;
import com.websocketim.manager.DialogManager;
import com.websocketim.manager.IMChatManager;
import com.websocketim.model.ChatMessage;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.MsgCache;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IMActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "IMActivity";
    public static boolean isShowing = false;
    public static Activity activity;

    private TextView title;
    private ImageView back;
    private ListView chatlist;
    private List<ChatMessage> recentList = new ArrayList<ChatMessage>();

    private RecentListAdapter adapter = new RecentListAdapter();

    private DisplayImageOptions options = null;
    private String master = "";
    private String avator = "";

    //新消息
    private NewMsgReceiver newMsgReceiver = null;
    private LocalBroadcastManager localBroadcastManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_main);
        activity = this;

        int ic_head_defaultId = R.drawable.ic_head_default;
        this.options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageForEmptyUri(ic_head_defaultId)
                .showImageOnFail(ic_head_defaultId)
                .showImageOnLoading(ic_head_defaultId)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .build();

        UserInfo userinfo = (UserInfo) MsgCache.get(IMActivity.this).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
            avator = userinfo.getHead50();
        }

        title = (TextView) findViewById(R.id.title);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        chatlist = (ListView) findViewById(R.id.chat_list);
        chatlist.setAdapter(adapter);
        chatlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ChatMessage cm = (ChatMessage) adapterView.getItemAtPosition(position);
                IMChatManager.startChat(IMActivity.this, master,avator, cm.getType(), cm.getFromuser(), cm.getFromusernick());
                if (cm.getUnreadCount() > 0) {
                    cm.setUnreadCount(0);
                    adapter.notifyDataSetChanged();
                }

                // 未读消息数改为0
                DBHelper helper = new DBHelper(getApplication());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("UPDATE chat_recent SET msg_unread_count=0 WHERE username='" + master + "' " +
                                "AND msg_type='" + cm.getType() + "' " +
                                "AND msg_content=? " +
                                "AND msg_time='" + cm.getTime() + "' " +
                                "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))",
                        new Object[]{cm.getContent()});
                db.close();

            }
        });
        // 长按删除最近聊天
        this.chatlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                final ChatMessage cm = (ChatMessage) parent.getItemAtPosition(position);
                DialogManager.showDialog(IMActivity.this, "\n删除该聊天", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBHelper helper = new DBHelper(IMActivity.this);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        db.execSQL("DELETE FROM chat_recent WHERE username='" + master + "' " +
                                        "AND msg_type='" + cm.getType() + "' " +
                                        "AND msg_content=? " +
                                        "AND msg_time='" + cm.getTime() + "' " +
                                        "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))",
                                new Object[]{cm.getContent()});
                        // 1月16号版本要求删除最近聊天的同时删除聊天记录
                        db.execSQL("DELETE FROM chat_history WHERE username='" + master + "' " +
                                "AND msg_type='" + cm.getType() + "' " +
                                "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))");
                        db.close();

                        recentList.remove(cm);
                        adapter.notifyDataSetChanged();
                    }
                }, null, null);
                return true;
            }
        });
        this.localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        this.newMsgReceiver = new NewMsgReceiver();
        registerReceiver(newMsgReceiver, new IntentFilter(Constants.CHAT_NEW_MESSAGE));

    }

    @Override
    public void onResume() {
        super.onResume();
        isShowing = true;
        // 异步线程读取最近聊天列表
        new GetRecentListTask().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        isShowing = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != newMsgReceiver) {
            unregisterReceiver(newMsgReceiver);
        }
    }

    private boolean isLoading = false;

    @Override
    public void onClick(View view) {
        finish();
    }

    private class GetRecentListTask extends AsyncTask<Void, Void, List<ChatMessage>> {

        @Override
        protected void onPreExecute() {
            isLoading = true;
        }

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
            isLoading = false;
            recentList.clear();
            recentList.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }

    public class RecentListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return recentList.size();
        }

        @Override
        public Object getItem(int i) {
            return recentList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (null == convertView) {
                convertView = getLayoutInflater().inflate(R.layout.row_im_list_common_item, null);
                holder = new ViewHolder();
                holder.avator = convertView.findViewById(R.id.avatar);
                holder.nickname = convertView.findViewById(R.id.nickname);
                holder.content = convertView.findViewById(R.id.content);
                holder.unreadTip = convertView.findViewById(R.id.unread_tip);
                holder.time = convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ChatMessage cm = (ChatMessage) getItem(position);
            // 列表上显示的名字
            // 优先取用户名对应的contact title
            // 为空去nickname
            // 再为空就取from
            String titleNick = cm.getFromusernick();
            if (CommonUtil.isBlank(titleNick)) {
                titleNick = cm.getFromuser();
                if (CommonUtil.isBlank(titleNick)) {
                    titleNick = cm.getFromuser();
                }
            }
            holder.nickname.setText(titleNick);
            holder.content.setText(cm.getContentDescr());
//            if (cm.getType().equals(Constants.FRAGMENT_FRIEND)){
//                holder.descr.setText(cm.getContentDescr());
//            }else if (cm.getType().equals(Constants.FRAGMENT_GROUP)){
//                Log.d(TAG, "getView: "+master+"   "+cm.getUsername());
//                if (master.equals(cm.getUsername())) {
//                    holder.descr.setText(cm.getContentDescr());
//                }else {
//                    holder.descr.setText(cm.getNickname() + ":" + cm.getContentDescr());
//                }
//            }
            ImageLoader.getInstance().displayImage(cm.getAvatar(), holder.avator, options);

            DateFormat dfHour = new SimpleDateFormat("HH:mm");
            DateFormat dfDay = new SimpleDateFormat("MM月dd日");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date cur = new Date();
                Date chatTime = df.parse(cm.getTime());
                long diff = cur.getTime() - chatTime.getTime();
                if (diff / (1000 * 60 * 60 * 24) >= 1) {
                    holder.time.setText(dfDay.format(chatTime));
                } else {
                    holder.time.setText(dfHour.format(chatTime));
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.time.setText(dfHour.format(new Date()));
            }

            holder.unreadTip.setText(cm.getUnreadCount() + "");
            if (cm.getUnreadCount() == 0) {
                holder.unreadTip.setVisibility(View.GONE);
            } else {
                holder.unreadTip.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

    }

    static class ViewHolder {
        ImageView avator;
        TextView nickname;
        TextView content;
        TextView unreadTip;
        TextView time;
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

}

