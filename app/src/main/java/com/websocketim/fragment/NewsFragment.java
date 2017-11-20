package com.websocketim.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/11/1.
 */

public class NewsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "NewsFragment";

    public static boolean isShowing = false;
    public static Activity activity;

    private ListView chatlist;
    private List<ChatMessage> recentList = new ArrayList<ChatMessage>();

    private RecentListAdapter adapter = new RecentListAdapter();

    private DisplayImageOptions options = null;
    private String master = "";
    private String avatar = "";

    //新消息
    private NewMsgReceiver newMsgReceiver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, null);

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

        UserInfo userinfo = (UserInfo) MsgCache.get(getContext()).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
            avatar = userinfo.getHead50();
        }

        chatlist = (ListView) rootView.findViewById(R.id.chat_list);
        chatlist.setAdapter(adapter);
        chatlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ChatMessage cm = (ChatMessage) adapterView.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + cm.getFromuser());
                IMChatManager.startChat(getActivity(), master, avatar,cm.getType(), cm.getFromuser(), cm.getFromusernick());
                if (cm.getUnreadCount() > 0) {
                    cm.setUnreadCount(0);
                    adapter.notifyDataSetChanged();
                }

                // 未读消息数改为0
                DBHelper helper = new DBHelper(getActivity());
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
                DialogManager.showDialog(getActivity(), "\n删除该聊天", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBHelper helper = new DBHelper(getActivity());
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
        this.newMsgReceiver = new NewMsgReceiver();
        getActivity().registerReceiver(newMsgReceiver, new IntentFilter(Constants.CHAT_NEW_MESSAGE));

        return rootView;
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
    public void onDestroy() {
        super.onDestroy();
        if (null != newMsgReceiver) {
            getActivity().unregisterReceiver(newMsgReceiver);
        }
    }

    private boolean isLoading = false;

    @Override
    public void onClick(View view) {
    }

    private class GetRecentListTask extends AsyncTask<Void, Void, List<ChatMessage>> {

        @Override
        protected void onPreExecute() {
            isLoading = true;
        }

        @Override
        protected List<ChatMessage> doInBackground(Void... params) {
            Context cxt = getActivity();
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

        private HashMap<String, String> emotionKV = new HashMap<String, String>();

        public RecentListAdapter() {
            // init emotionmap
            SQLiteDatabase emotionDatabase = DBHelper.getEmotionDatabase();
            Cursor cur = emotionDatabase.rawQuery("select * from emotion", null);
            while (cur.moveToNext()) {
                String emotionDescr = cur.getString(cur.getColumnIndex("e_descr"));
                String emotionFilename = cur.getString(cur.getColumnIndex("e_name"));
                Log.d(TAG, "RecentListAdapter: "+emotionDescr+"  "+emotionFilename);
                emotionKV.put(emotionDescr, emotionFilename);
            }
            cur.close();
            emotionDatabase.close();
        }

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
                convertView = getLayoutInflater(getArguments()).inflate(R.layout.row_im_list_common_item, null);
                holder = new ViewHolder();
                holder.avator = convertView.findViewById(R.id.avatar);
                holder.nickname = convertView.findViewById(R.id.nickname);
                holder.content = convertView.findViewById(R.id.chatContent);
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
            
            SpannableString showSS = null;
            if (cm.getType().equals(Constants.FRAGMENT_FRIEND)) {
                if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT)) {
                    showSS = getExpressionString(cm.getContent());
                    holder.content.setText(showSS);
                }else {
                    holder.content.setText(cm.getContentDescr());
                }
            } else if (cm.getType().equals(Constants.FRAGMENT_GROUP)) {
                if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT)) {
                    showSS = getExpressionString(cm.getNickname() + ": " +cm.getContent());
                    holder.content.setText(showSS);
                }else {
                    holder.content.setText(cm.getNickname() + ": " +cm.getContentDescr());
                }
            }


            DateFormat oldDay = new SimpleDateFormat("dd");
            DateFormat newDay = new SimpleDateFormat("dd");
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
                    if (Integer.parseInt(oldDay.format(chatTime)) < Integer.parseInt(newDay.format(new Date()))) {
                        holder.time.setText(dfDay.format(chatTime));
                    } else {
                        holder.time.setText(dfHour.format(chatTime));
                    }
//                    holder.time.setText(dfHour.format(chatTime));
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

            ImageLoader.getInstance().displayImage(cm.getAvatar(), holder.avator, options);

            return convertView;
        }

        private SpannableString getExpressionString(String str) {
            SpannableString spannableString = new SpannableString(str);
            // 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
//	        String zhengze = "\\[[^\\]]+\\]";
            String zhengze = "\\[[\\u4e00-\\u9fa5a-z]{1,3}\\]";
            // 通过传入的正则表达式来生成一个pattern
            Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
            try {
                dealExpression(spannableString, sinaPatten, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return spannableString;
        }

        private void dealExpression(SpannableString spannableString, Pattern patten, int start) throws Exception {
            Matcher matcher = patten.matcher(spannableString);
            while (matcher.find()) {
                String key = matcher.group();
                // 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
                if (matcher.start() < start) {
                    continue;
                }
                String value = emotionKV.get(key);

                if (CommonUtil.isBlank(value)) {
                    continue;
                }
                Drawable d = Drawable.createFromStream(getActivity().getAssets().open("emotion/" + value), null);
                int size = CommonUtil.sp2px(getActivity(), 16);
                d.setBounds(0, 0, size, size);
                ImageSpan imageSpan = new ImageSpan(d);
                // ImageSpan imageSpan = new ImageSpan(bitmap);
                // 计算该图片名字的长度，也就是要替换的字符串的长度
                int end = matcher.start() + key.length();
                // 将该图片替换字符串中规定的位置中
                spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                if (end < spannableString.length()) {
                    // 如果整个字符串还未验证完，则继续。。
                    dealExpression(spannableString, patten, end);
                }
                break;
            }

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