package com.websocketim.fragment;

import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.websocketim.Constants;
import com.websocketim.DBHelper;
import com.websocketim.R;
import com.websocketim.activity.SendRedPacketActivity;
import com.websocketim.activity.UserInfoPreviewActivity;
import com.websocketim.asynchttp.ResultData;
import com.websocketim.asynchttp.ResultManager;
import com.websocketim.manager.IMChatManager;
import com.websocketim.manager.PlaySoundManager;
import com.websocketim.model.ChatMessage;
import com.websocketim.service.SocketService;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.FileUtils;
import com.websocketim.utils.GlideLoader;
import com.websocketim.utils.ToastUtils;
import com.websocketim.view.RelativeLayoutHasResizeListener;
import com.websocketim.view.VoiceRecorderView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.animation.ObjectAnimator.ofPropertyValuesHolder;
import static android.app.Activity.RESULT_OK;
import static com.websocketim.R.id.sound_container;

/**
 * Created by Administrator on 2017/11/2.
 */

public class RedPacketRoomFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "RedPacketRoomFragment";
    private String avatorUrl = "";

    private ListView chat_list;
    // 是否在load历史聊天记录
    private boolean isLoadingMore = false;
    private ChatMessageAdapter mAdapter;
    private List<ChatMessage> chatMsgs = new ArrayList<ChatMessage>();
    private HashMap<String, String> emotionKV = new HashMap<String, String>();
    // activity是否可见
    public static boolean isShowing = false;
    // activity不可见的时候收到的消息
    private List<ChatMessage> chatMsgOnPause = new ArrayList<ChatMessage>();

    // 战绩的container
    private View standingsContainer = null;
    // 发送声音的container
    private View soundContainer = null;
    // 发红包container
    private View sendRedContainer = null;
    //录制声音音量
    private VoiceRecorderView voiceRecorderView;
    // 发送声音
    // 播放声音
    private MediaPlayer mediaPlayer = null;

    private MediaRecorder mediaRecorder = null;

    private boolean isRecording = false;

    private String curSoundPath = "";
    // 开始录制的时间
    private long recordStartTime = 0;
    // 当前登录的用户名
    private String master = "";
    // 聊天对象
    private String friend = "";
    private String friendNick = "";
    // 聊天类型 chat或者groupchat
    private String chatType = "";

    // 头像的option
    private DisplayImageOptions options = null;

    //新消息
    private NewMsgReceiver newMsgReceiver = null;
    //聊天
    private ChatReceiver chatReceiver = null;

    private String curPlayUrl = "";

    private static final int REQUEST_USER_SEND_RED = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_redpacket_room, null);

        int ic_head_defaultId = R.drawable.ic_head_default;
        this.options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageForEmptyUri(ic_head_defaultId)
                .showImageOnFail(ic_head_defaultId)
                .showImageOnLoading(ic_head_defaultId)
                .displayer(new SimpleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .build();

        this.mediaPlayer = new MediaPlayer();
        this.chat_list = (ListView) rootView.findViewById(R.id.chat_list);
        this.soundContainer = rootView.findViewById(sound_container);
        this.sendRedContainer = rootView.findViewById(R.id.sendred_container);
        this.standingsContainer = rootView.findViewById(R.id.standings_container);
        this.voiceRecorderView = (VoiceRecorderView) rootView.findViewById(R.id.voice_recorder);

        this.soundContainer.setOnClickListener(this);
        this.sendRedContainer.setOnClickListener(this);
        this.standingsContainer.setOnClickListener(this);

        initData();

        // 输入法弹出，列表自动滚到底部
        RelativeLayoutHasResizeListener hasResizeListener = (RelativeLayoutHasResizeListener) rootView.findViewById(R.id.root_layout);
        hasResizeListener.setOnResizeListener(new RelativeLayoutHasResizeListener.OnResizeListener() {
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                scrollToBottom();
            }
        });

        this.chat_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        }
                        if (view.getFirstVisiblePosition() == 0 && !isLoadingMore) {
                            isLoadingMore = true;
                            new LoadMoreChat().execute();
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });

        this.soundContainer.setOnTouchListener(new View.OnTouchListener() {

            private final static int WHAT_START = 1;
            private final static int WHAT_STOP = 2;
            private final static int WHAT_TIME = 3;
            private final static int SHOW_VOICE_RECORDER_VIEW = 4;
            private final static int WHAT_STOP_UNSAVE = 5;

            private Timer timer = null;
            private int seconds = 0;

            private Handler handler = new Handler() {

                @Override
                public void handleMessage(android.os.Message msg) {
                    switch (msg.what) {
                        case WHAT_START:
                            if (!isActionUp) {
                                if (null != mediaPlayer && mediaPlayer.isPlaying()) {
                                    curPlayUrl = "";
                                    mAdapter.notifyDataSetChanged();
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaPlayer = null;
                                }
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    int t = 0;

                                    @Override
                                    public void run() {
                                        if (t >= 60) {
                                            timer.cancel();
                                            handler.sendEmptyMessage(WHAT_STOP);
                                            return;
                                        }
                                        Message msg = new Message();
                                        msg.what = WHAT_TIME;
                                        msg.obj = t;
                                        handler.sendMessage(msg);
                                        t++;
                                    }
                                }, 0, 1000);
                                startRecordSound();
                            }
                            break;
                        case WHAT_STOP:
                            if (seconds > 0) {
                                stopRecordSound();
                            } else {
                                stopRecordSoundUnSave();
                                Toast.makeText(getActivity(), "录音时间太短", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case WHAT_STOP_UNSAVE:
                            stopRecordSoundUnSave();
                            break;
                        case WHAT_TIME:
                            seconds = (int) msg.obj;
                            break;
                        case SHOW_VOICE_RECORDER_VIEW:
                            initVoiceRecorderView();
                            break;
                        default:
                            break;
                    }
                }
            };

            // 是否抬起
            private boolean isActionUp = true;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isActionUp = false;
                        handler.sendEmptyMessage(WHAT_START);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() < -80) {
                            voiceRecorderView.showReleaseToCancelHint();
                        } else {
                            voiceRecorderView.showMoveUpToCancelHint();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        isActionUp = true;
                        if (null != timer) {
                            timer.cancel();
                        }
                        if (event.getY() < -80) {
                            handler.sendEmptyMessage(WHAT_STOP_UNSAVE);
                        } else {
                            handler.sendEmptyMessage(WHAT_STOP);
                        }
                        voiceRecorderView.showMoveUpToCancelHint();
                        break;

                    default:
                        break;
                }
                return false;
            }
        });

        chatReceiver = new ChatReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.SEND_CHATMESSAGE_SUCCESS);
        intentFilter.addAction(Constants.SEND_CHATMESSAGE_FAIL);
        getActivity().registerReceiver(chatReceiver, intentFilter);

        newMsgReceiver = new NewMsgReceiver();
        getActivity().registerReceiver(newMsgReceiver, new IntentFilter(Constants.CHAT_NEW_MESSAGE));

        return rootView;
    }

    public interface OnRedFragmenClickListener {

        public void onOnRedFragmentClicked(Object obj);
    }

    private OnRedFragmenClickListener clickListener = null;

    public void setOnRedFragmenClickListener(OnRedFragmenClickListener l) {
        if (null != l) {
            this.clickListener = l;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendred_container:
                Intent intent = new Intent(getActivity(), SendRedPacketActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_USER_SEND_RED);
                break;
            case R.id.standings_container:
                clickListener.onOnRedFragmentClicked("战绩");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_USER_SEND_RED:
                    if (null != data){
                        Bundle bundle = data.getExtras();
                        if (null != bundle){
                            ChatMessage cm = (ChatMessage) bundle.getSerializable("redinfor");
                            Log.d(TAG,"content"+cm.getContent());
                            doSend(cm);
                        }
                    }
                    break;
            }
        }
    }

    // 新消息广播接收器
    private class NewMsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent && intent.getAction().equals(Constants.CHAT_NEW_MESSAGE)) {
                // 接收新消息进行显示
                ChatMessage cm = new ChatMessage();
                cm.setContent(intent.getStringExtra("msg_content"));
                cm.setFromuser(intent.getStringExtra("msg_from"));
                cm.setFromusernick(intent.getStringExtra("msg_fromnick"));
                cm.setTouser(intent.getStringExtra("msg_to"));
                cm.setTousernick(intent.getStringExtra("msg_tonick"));
                cm.setTime(intent.getStringExtra("msg_time"));
                cm.setClientId(intent.getStringExtra("msg_clientId"));
                cm.setType(intent.getStringExtra("msg_type"));
                cm.setAvatar(intent.getStringExtra("msg_avatar"));
                cm.setNickname(intent.getStringExtra("msg_nickname"));
                cm.setUsername(intent.getStringExtra("msg_username"));
                cm.setUrl(intent.getStringExtra("msg_media_url"));
                cm.setExtra(intent.getStringExtra("msg_media_extra"));
                cm.setThumbnailUrl(intent.getStringExtra("msg_media_thumbnail"));
                cm.setContentType(intent.getStringExtra("msg_content_type"));

                if (isUsable() && null != mAdapter) {
                    // 判断聊天对象
                    if (cm.getFromuser().equals(friend) && cm.getType().equals(chatType)) {
                        // 更新聊天列表
                        chatMsgs.add(cm);
                        mAdapter.notifyDataSetChanged();
                        scrollToBottom();
                        if (isShowing) {
                            // 将信息更新为已读状态
                            read(cm);
                        } else {
                            // 将信息加入activity不可见情况下的列表
                            chatMsgOnPause.add(cm);
                        }
                    }
                }

            }
        }
    }

    private class ChatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.d(TAG, "onReceive: action " + action);
                if (action.equals(Constants.SEND_CHATMESSAGE_SUCCESS)) {
                    Bundle bundle = intent.getExtras();
                    if (null != bundle) {
                        ChatMessage chatmsg = (ChatMessage) bundle.getSerializable("ChatMessage");
//                        chatmsg.setProgress("false");
                        IMChatManager.getInstance(getActivity()).updateSendSuccess(chatmsg);
                        if (chatmsg.getTouser().equals(friend) && chatmsg.getType().equals(chatType)) {
                            for (ChatMessage cm : chatMsgs) {
                                if (cm.getClientId().equals(chatmsg.getClientId())) {
                                    cm.setResend("false");
                                    cm.setProgress("false");
                                    mAdapter.notifyDataSetChanged();
                                    scrollToBottom();
                                }
                            }
                        }
                    }
                } else if (action.equals(Constants.SEND_CHATMESSAGE_FAIL)) {
                    Bundle bundle = intent.getExtras();
                    if (null != bundle) {
                        ChatMessage chatmsg = (ChatMessage) bundle.getSerializable("ChatMessage");
                        if (chatmsg.getTouser().equals(friend) && chatmsg.getType().equals(chatType)) {
                            for (ChatMessage cm : chatMsgs) {
                                if (cm.getClientId().equals(chatmsg.getClientId())) {
                                    cm.setPrompt("true");
                                    cm.setResend("false");
                                    cm.setProgress("false");
                                    mAdapter.notifyDataSetChanged();
                                    scrollToBottom();
                                }
                            }
                        }
                        ToastUtils.showShort(getActivity(), "发送失败,");
                    }
                }
            }
        }
    }

    // 加载更多聊天记录的task
    private class LoadMoreChat extends AsyncTask<Void, Void, List<ChatMessage>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<ChatMessage> doInBackground(Void... params) {
            int curCount = mAdapter.getCount();
            int limit = curCount + 20;
            DBHelper helper = new DBHelper(getActivity());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cur = db.rawQuery("SELECT * FROM (SELECT * FROM chat_history WHERE username='" + master + "' AND msg_type='" + chatType + "' AND ((msg_from='" + master + "' AND msg_to='" + friend + "') OR (msg_from='" + friend + "' AND msg_to='" + master + "')) ORDER BY _id DESC LIMIT " + curCount + "," + limit + ") ORDER BY msg_time", null);
            List<ChatMessage> moreCms = new ArrayList<ChatMessage>();
            while (cur.moveToNext()) {
                ChatMessage cm = new ChatMessage();
                String from = cur.getString(cur.getColumnIndex("msg_from"));
                String fromnick = cur.getString(cur.getColumnIndex("msg_fromnick"));
                String to = cur.getString(cur.getColumnIndex("msg_to"));
                String tonick = cur.getString(cur.getColumnIndex("msg_tonick"));
                String content = cur.getString(cur.getColumnIndex("msg_content"));
                String time = cur.getString(cur.getColumnIndex("msg_time"));
                String clientId = cur.getString(cur.getColumnIndex("msg_clientId"));
                String nickname = cur.getString(cur.getColumnIndex("msg_nickname"));
                String username = cur.getString(cur.getColumnIndex("msg_username"));
                String avatar = cur.getString(cur.getColumnIndex("msg_avatar"));
                String url = cur.getString(cur.getColumnIndex("msg_media_url"));
                String extra = cur.getString(cur.getColumnIndex("msg_media_extra"));
                String thumbnail = cur.getString(cur.getColumnIndex("msg_media_thumbnail"));
                String audioState = cur.getString(cur.getColumnIndex("msg_audio_state"));
                String prompt = cur.getString(cur.getColumnIndex("msg_prompt"));
                String contentType = cur.getString(cur.getColumnIndex("msg_content_type"));

                cm.setUsername(username);
                cm.setNickname(nickname);
                cm.setAvatar(avatar);
                cm.setFromuser(from);
                cm.setFromusernick(fromnick);
                cm.setTouser(to);
                cm.setTousernick(tonick);
                cm.setContent(content);
                cm.setTime(time);
                cm.setClientId(clientId);
                cm.setUrl(url);
                cm.setExtra(extra);
                cm.setThumbnailUrl(thumbnail);
                cm.setAudioState(audioState);
                cm.setPrompt(prompt);
                cm.setContentType(contentType);
                moreCms.add(cm);
            }
            cur.close();
            db.close();
            return moreCms;
        }

        @Override
        protected void onPostExecute(List<ChatMessage> result) {
            isLoadingMore = false;
//            if (!isFinishing()) {
            List<ChatMessage> more = result;
            if (more.size() > 0) {
                chatMsgs.addAll(0, more);
                mAdapter.notifyDataSetChanged();
                chat_list.setSelection(more.size());

            }
//            }
        }
    }

    // 将聊天列表滚动到底端
    private void scrollToBottom() {
        if (null != chat_list) {
            chat_list.post(new Runnable() {
                @Override
                public void run() {
                    chat_list.setSelection(chat_list.getBottom());
                }
            });
        }
    }

    private void initData() {
        Log.d(TAG, "initData: 1 ");
        Bundle bundle = getArguments();
        if (null != bundle) {
//            if (!CommonUtil.isBlank(friend) && !CommonUtil.isBlank(master)) {
//                return;
//            }
            this.master = bundle.getString("master");
            this.avatorUrl = bundle.getString("avator");
            this.friend = bundle.getString("chat_friend");
            this.friendNick = bundle.getString("chat_friendNick");
            this.chatType = bundle.getString("chat_type");
        }

        chatMsgs = new ArrayList<ChatMessage>();
        // 取出最近20条聊天记录
        DBHelper helper = new DBHelper(getActivity());
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM (SELECT * FROM chat_history WHERE username='" + master + "' AND msg_type='" + chatType + "' AND ((msg_from='" + master + "' AND msg_to='" + friend + "') OR (msg_from='" + friend + "' AND msg_to='" + master + "')) ORDER BY _id DESC LIMIT 0,20) ORDER BY msg_time", null);
        chatMsgs.clear();
        while (cur.moveToNext()) {
            ChatMessage cm = new ChatMessage();
            String master = cur.getString(cur.getColumnIndex("username"));
            String from = cur.getString(cur.getColumnIndex("msg_from"));
            String fromnick = cur.getString(cur.getColumnIndex("msg_fromnick"));
            String to = cur.getString(cur.getColumnIndex("msg_to"));
            String tonick = cur.getString(cur.getColumnIndex("msg_tonick"));
            String content = cur.getString(cur.getColumnIndex("msg_content"));
            String time = cur.getString(cur.getColumnIndex("msg_time"));
            String clientId = cur.getString(cur.getColumnIndex("msg_clientId"));
            String nickname = cur.getString(cur.getColumnIndex("msg_nickname"));
            String username = cur.getString(cur.getColumnIndex("msg_username"));
            String type = cur.getString(cur.getColumnIndex("msg_type"));
            String avatar = cur.getString(cur.getColumnIndex("msg_avatar"));
            String url = cur.getString(cur.getColumnIndex("msg_media_url"));
            String extra = cur.getString(cur.getColumnIndex("msg_media_extra"));
            String thumbnail = cur.getString(cur.getColumnIndex("msg_media_thumbnail"));
            String audioState = cur.getString(cur.getColumnIndex("msg_audio_state"));
            String prompt = cur.getString(cur.getColumnIndex("msg_prompt"));
            String contentType = cur.getString(cur.getColumnIndex("msg_content_type"));

            cm.setMaster(master);
            cm.setNickname(nickname);
            cm.setUsername(username);
            cm.setType(type);
            cm.setAvatar(avatar);
            cm.setFromuser(from);
            cm.setFromusernick(fromnick);
            cm.setTouser(to);
            cm.setTousernick(tonick);
            cm.setContent(content);
            cm.setTime(time);
            cm.setClientId(clientId);
            cm.setUrl(url);
            cm.setExtra(extra);
            cm.setThumbnailUrl(thumbnail);
            cm.setAudioState(audioState);
            cm.setPrompt(prompt);
            cm.setContentType(contentType);
            chatMsgs.add(cm);
        }
        // 将所有未读消息变成已读状态
        db.execSQL("UPDATE chat_history SET msg_state='read' WHERE username='" + master + "' AND msg_type='" + chatType + "' AND msg_state='unread' AND ((msg_from='" + master + "' AND msg_to='" + friend + "') OR (msg_from='" + friend + "' AND msg_to='" + master + "'))");
        cur.close();
        db.close();

        mAdapter = new ChatMessageAdapter(chatMsgs);
        chat_list.setAdapter(mAdapter);

    }

    //适配器
    private class ChatMessageAdapter extends BaseAdapter {

        private final static int VIEW_TYPE_COUNT = 2;

        private final static int VIEW_TYPE_SEND = 1;
        private final static int VIEW_TYPE_REV = 0;

        private List<ChatMessage> list;
        private PlaySoundManager playSoundManager = null;
        private int curPlayPosition = -1;

        public ChatMessageAdapter(List<ChatMessage> mlist) {
            if (mlist != null) {
                this.list = mlist;
            } else {
                this.list = new ArrayList<ChatMessage>();
            }
            this.playSoundManager = new PlaySoundManager(getActivity(), mediaPlayer, master);
            // init emotionmap
            SQLiteDatabase emotionDatabase = DBHelper.getEmotionDatabase();
            Cursor cur = emotionDatabase.rawQuery("select * from emotion", null);
            while (cur.moveToNext()) {
                String emotionDescr = cur.getString(cur.getColumnIndex("e_descr"));
                String emotionFilename = cur.getString(cur.getColumnIndex("e_name"));
                emotionKV.put(emotionDescr, emotionFilename);
            }
            cur.close();
            emotionDatabase.close();
        }

        public void setChatMessageData(List<ChatMessage> mlist) {
            this.list = mlist;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage chatMessage = (ChatMessage) getItem(position);
            if (chatMessage.getFromuser().equals(master)) {
                return VIEW_TYPE_SEND;
            } else {
                return VIEW_TYPE_REV;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolderRev holderRev = null;
            ViewHolderSend holderSend = null;
            if (convertView == null) {
                int rev_layoutId = R.layout.row_receive_red;
                int send_layoutId = R.layout.row_send_red;
                int avatarId = R.id.avatar;
                int nicknameId = R.id.nickname;
                int timeId = R.id.time;
                int contentId = R.id.content;
                int sound_timeId = R.id.sound_time;
                int sound_wave_showId = R.id.sound_wave_show;
                int promptId = R.id.prompt;
                int progressId = R.id.progress;
                int containerId = R.id.container;
                int red_containerId = R.id.red_container;
                int sound_containerId = R.id.sound_container;
                switch (getItemViewType(position)) {
                    case VIEW_TYPE_REV:
                        convertView = getLayoutInflater(getArguments()).inflate(rev_layoutId, null);
                        holderRev = new ViewHolderRev();
                        holderRev.avatar = convertView.findViewById(avatarId);
                        holderRev.nickname = convertView.findViewById(nicknameId);
                        holderRev.time = convertView.findViewById(timeId);
                        holderRev.content = convertView.findViewById(contentId);
                        holderRev.soundTime = convertView.findViewById(sound_timeId);
                        holderRev.soundWave = convertView.findViewById(sound_wave_showId);
                        holderRev.container = convertView.findViewById(containerId);
                        holderRev.redContainer = convertView.findViewById(red_containerId);
                        holderRev.soundContainer = convertView.findViewById(sound_containerId);
                        convertView.setTag(holderRev);
                        break;
                    case VIEW_TYPE_SEND:
                        convertView = getLayoutInflater(getArguments()).inflate(send_layoutId, null);
                        holderSend = new ViewHolderSend();
                        holderSend.avatar = convertView.findViewById(avatarId);
                        holderSend.nickname = convertView.findViewById(nicknameId);
                        holderSend.time = convertView.findViewById(timeId);
                        holderSend.content = convertView.findViewById(contentId);
                        holderSend.soundTime = convertView.findViewById(sound_timeId);
                        holderSend.soundWave = convertView.findViewById(sound_wave_showId);
                        holderSend.prompt = convertView.findViewById(promptId);
                        holderSend.progress = convertView.findViewById(progressId);
                        holderSend.container = convertView.findViewById(containerId);
                        holderSend.redContainer = convertView.findViewById(red_containerId);
                        holderSend.soundContainer = convertView.findViewById(sound_containerId);
                        convertView.setTag(holderSend);
                        break;

                    default:
                        break;
                }
            } else {
                switch (getItemViewType(position)) {
                    case VIEW_TYPE_REV:
                        holderRev = (ViewHolderRev) convertView.getTag();
                        break;
                    case VIEW_TYPE_SEND:
                        holderSend = (ViewHolderSend) convertView.getTag();
                        break;
                    default:
                        break;
                }
            }
            final ChatMessage msg = (ChatMessage) getItem(position);

            SpannableString showSS = null;
            if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT)) {
                showSS = getExpressionString(msg.getContent());
            }

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat dfDay = new SimpleDateFormat("MM月dd日 HH:mm");
            Date chatTime = null;
            try {
                chatTime = df.parse(msg.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            switch (getItemViewType(position)) {
                case VIEW_TYPE_REV:
                    if (msg.getType().equals(Constants.FRAGMENT_GROUP)) {
                        holderRev.nickname.setVisibility(View.VISIBLE);
                    }
                    holderRev.nickname.setText(msg.getNickname());
                    holderRev.content.setText(showSS);
                    holderRev.time.setText(dfDay.format(chatTime));
                    holderRev.time.setVisibility(View.GONE);
                    if (position == 0) {
                        holderRev.time.setVisibility(View.VISIBLE);
                    } else {
                        try {
                            Date pre = df.parse(((ChatMessage) getItem(position - 1)).getTime());
                            Date cur = df.parse(msg.getTime());
                            long diff = cur.getTime() - pre.getTime();
                            // 间隔大于一分钟显示
                            if (diff / (1000 * 60) > 1) {
                                holderRev.time.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    holderRev.redContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            robRedPacketDialog(msg, position);
                        }
                    });

                    if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                        holderRev.soundContainer.setVisibility(View.VISIBLE);
                        if (msg.getAudioState().equals("unread")){
                            ChatMessage cm = (ChatMessage) getItem(position);
                            cm.setAudioState("read");
                            IMChatManager.getInstance(getActivity()).updateAudioState(msg);
                        }else {
                        }
                        holderRev.soundContainer.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                curPlayUrl = msg.getUrl();
                                notifyDataSetChanged();
                                playSoundManager.play(msg.getUrl(), new PlaySoundManager.PlaySoundFinishedListener() {

                                    @Override
                                    public void onSoundFinished() {
                                        curPlayUrl = "";
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                        holderRev.soundTime.setVisibility(View.VISIBLE);
                        holderRev.soundTime.setText(msg.getExtra() + "''");
                        holderRev.soundWave.setVisibility(View.VISIBLE);
                        if (curPlayUrl.equals(msg.getUrl())) {
                            AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.bg_redpacket_sound_wave);
                            holderRev.soundWave.setBackgroundDrawable(animationDrawable);
                            animationDrawable.start();
                        } else {
                            holderRev.soundWave.setBackgroundResource(R.drawable.ic_redpacket_sound_wave_3);
                        }
                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                        holderRev.soundContainer.setVisibility(View.GONE);
                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                        holderRev.soundContainer.setVisibility(View.GONE);
                    } else {
                        holderRev.soundContainer.setVisibility(View.GONE);
                        holderRev.content.setOnClickListener(null);
                        holderRev.soundWave.setVisibility(View.GONE);
                        holderRev.soundTime.setVisibility(View.GONE);
                        holderRev.soundTime.setText("");
                    }

                    holderRev.avatar.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("master", master);
                            bundle.putString("username", msg.getUsername());
                            bundle.putString("nickname", msg.getNickname());
                            bundle.putString("avatar", msg.getAvatar());
                            Intent intent = new Intent(getActivity(), UserInfoPreviewActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    GlideLoader.LoderAvatar(getActivity(),msg.getAvatar(),holderRev.avatar,10);
                    holderRev.time.setVisibility(View.GONE);
                    break;
                case VIEW_TYPE_SEND:
                    int ic_exclamation_markId = R.drawable.ic_exclamation_mark;
                    int bg_transparentId = R.drawable.bg_transparent;
                    if (!CommonUtil.isBlank(msg.getPrompt()) && msg.getPrompt().equals("true")) {
                        if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                            holderSend.prompt.setImageResource(bg_transparentId);
                        } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                            holderSend.prompt.setImageResource(ic_exclamation_markId);
                        }
                    } else {
                        holderSend.prompt.setImageResource(bg_transparentId);
                    }

                    if (!CommonUtil.isBlank(msg.getProgress()) && msg.getProgress().equals("true")) {
                        if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                            holderSend.progress.setVisibility(View.GONE);
                        } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                            holderSend.progress.setVisibility(View.VISIBLE);
                        }
                    } else {
                        holderSend.progress.setVisibility(View.GONE);
                    }

                    holderSend.content.setText(showSS);
                    holderSend.time.setText(dfDay.format(chatTime));
                    holderSend.time.setVisibility(View.GONE);
                    if (position == 0) {
                        holderSend.time.setVisibility(View.VISIBLE);
                    } else {
                        try {
                            Date pre = df.parse(((ChatMessage) getItem(position - 1)).getTime());
                            Date cur = df.parse(msg.getTime());
                            long diff = cur.getTime() - pre.getTime();
                            // 间隔大于一分钟显示
                            if (diff / (1000 * 60) > 1) {
                                holderSend.time.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    holderSend.redContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            robRedPacketDialog(msg, position);
                        }
                    });
                    if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                        holderSend.soundContainer.setVisibility(View.VISIBLE);
                        holderSend.soundContainer.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                curPlayUrl = msg.getUrl();
                                curPlayPosition = position;
                                notifyDataSetChanged();
                                playSoundManager.play(msg.getUrl(), new PlaySoundManager.PlaySoundFinishedListener() {

                                    @Override
                                    public void onSoundFinished() {
                                        curPlayUrl = "";
                                        curPlayPosition = -1;
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                        holderSend.soundTime.setVisibility(View.VISIBLE);
                        holderSend.soundTime.setText(msg.getExtra() + "''");
                        holderSend.soundWave.setVisibility(View.VISIBLE);
                        if (curPlayUrl.equals(msg.getUrl()) && curPlayPosition == position) {
                            AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.bg_im_chat_sound_wave_on_text_right);
                            holderSend.soundWave.setBackgroundDrawable(animationDrawable);
                            animationDrawable.start();
                        } else {
                            holderSend.soundWave.setBackgroundResource(R.drawable.ic_im_chat_sound_wave_on_text_right_3);
                        }

                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                        holderSend.soundContainer.setVisibility(View.GONE);

                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                        holderSend.soundContainer.setVisibility(View.GONE);

                    } else {
                        holderSend.soundContainer.setVisibility(View.GONE);
                        holderSend.content.setOnClickListener(null);
                        holderSend.soundWave.setVisibility(View.GONE);
                        holderSend.soundTime.setVisibility(View.GONE);
                        holderSend.soundTime.setText("");
                    }
                    GlideLoader.LoderAvatar(getActivity(),avatorUrl,holderSend.avatar,10);
                    holderSend.time.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            return convertView;
        }
    }

    private AlertDialog dialog;
    private AlertDialog dialogResult;
    private String url = "https://img6.bdstatic.com/img/image/smallpic/weijuchiluntu.jpg";

    private void robRedPacketDialog(final ChatMessage msg, final int position) {
        dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.view_rob_red_dialog_alert);
        window.getDecorView().setBackgroundColor(getResources().getColor(R.color.websocketim_transparent));

        View back = window.findViewById(R.id.back);
        CircleImageView avatar = window.findViewById(R.id.avatar);
        Glide.with(getActivity()).load(url).into(avatar);
        final View grab = window.findViewById(R.id.rl_grab);
        grab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grab.setEnabled(false);
                PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat("rotationY", 0.0f, 720.0f, 0.0f);
                ofPropertyValuesHolder(grab, pvh).setDuration(4000).start();
                new GrabRedpacketTask().execute();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

//        window.getDecorView().setPadding(80, 0, 80, 0);
//        window.getDecorView().setBackgroundResource(R.drawable.bg_button_red);
//        WindowManager windowManager = getActivity().getWindowManager();
//        Display display = windowManager.getDefaultDisplay();
//        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
//        lp.width = (int)(display.getWidth()*2/3); //设置宽度
//        window.setAttributes(lp);
    }

    public class GrabRedpacketTask extends AsyncTask<Object, Void, ResultData> {

        @Override
        protected ResultData doInBackground(Object... objects) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResultData resultData) {
            super.onPostExecute(resultData);
            dialog.cancel();
            Random rd = new Random();
            int i = rd.nextInt(3);
            grabRedResult(i);
        }

        private void grabRedResult(int i) {
            dialogResult = new AlertDialog.Builder(getActivity()).create();
            dialogResult.show();
            Window window = dialogResult.getWindow();
            window.setContentView(R.layout.view_red_result_dialog_alert);
            window.getDecorView().setBackgroundColor(getResources().getColor(R.color.websocketim_transparent));
            TextView result = window.findViewById(R.id.result);
            TextView state = window.findViewById(R.id.state);
            ImageView bomb = window.findViewById(R.id.bomb);
            View back = window.findViewById(R.id.back);
            if (i == 0) {
                result.setText("手慢了");
                state.setText("没抢到");
                bomb.setVisibility(View.GONE);
            } else if (i == 1) {
                result.setText("9.06");
                state.setText("已赔付50分");
                bomb.setVisibility(View.VISIBLE);
            } else if (i == 2) {
                result.setText("23.06");
                state.setText("已录入战绩");
                bomb.setVisibility(View.VISIBLE);
            }
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogResult.cancel();
                }
            });
        }
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
            int size = CommonUtil.sp2px(getActivity(), 20);
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

    static class ViewHolderRev {
        ImageView avatar;
        TextView nickname;
        TextView time;
        TextView content;
        TextView soundTime;
        ImageView soundWave;
        View container, redContainer, soundContainer;
    }

    static class ViewHolderSend {
        ImageView avatar;
        TextView nickname;
        TextView time;
        TextView content;
        TextView soundTime;
        ImageView soundWave;
        ImageView prompt;
        ProgressBar progress;
        View container, redContainer, soundContainer;
    }

    // 录制声音
    private void startRecordSound() {

        if (this.mediaRecorder != null) {
            this.mediaRecorder.release();
            this.mediaRecorder = null;
        }
        // 初始化mediaRecorder
        this.mediaRecorder = new MediaRecorder();
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置录音文件路径
        String fileName = "_" + System.currentTimeMillis() + "_AMR.amr";
        File file = FileUtils.createAttachmentFile(master,fileName);
        if (null != file && file.exists()) {
            curSoundPath = file.getPath();
            this.mediaRecorder.setOutputFile(curSoundPath);
            try {
                this.mediaRecorder.prepare();
                this.mediaRecorder.start();
                this.recordStartTime = System.currentTimeMillis();
                isRecording = true;
                initVoiceRecorderView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    // 创建一个附件文件
//    private File createAttachmentFile(String fileName) {
//        if (!CommonUtil.sdCardIsAvailable()) {
//            ToastUtils.showShort(getActivity(), "SD卡当前不可用");
//            return null;
//        }
//        if (CommonUtil.isBlank(fileName)) {
//            fileName = "_" + System.currentTimeMillis() + "_TMP";
//        }
//        // 例子
//        // /sdcard/msgcopy/<appName>/chat/<master>/<fileName>
//        String appName = getActivity().getPackageName();
//        String path = Environment.getExternalStorageDirectory() + File.separator +
//                "msgcopy" + File.separator +
//                appName + File.separator +
//                "chat" + File.separator +
//                master + File.separator +
//                fileName;
//
//        File file = new File(path);
//        File directory = file.getParentFile();
//        if (!directory.exists() && !directory.mkdirs()) {
//        }
//        try {
//            if (!file.exists()) {
//                file.createNewFile();
//            } else {
//                file.delete();
//                file.createNewFile();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return file;
//    }

    // 发送声音
    private void stopRecordSound() {
        voiceRecorderView.setVisibility(View.GONE);
        isRecording = false;
        if (null != mediaRecorder) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;

            String durTimeSec = (System.currentTimeMillis() - recordStartTime) / 1000 + "";
            recordStartTime = 0;

            File soundFile = new File(curSoundPath);
            if (soundFile.exists()) {
                ChatMessage cm = new ChatMessage();
                cm.setContent(soundFile.getName());
                cm.setUrl(soundFile.getPath());
                cm.setExtra(durTimeSec);
                cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_AUDIO);
                doSend(cm);
            }
        }
    }

    private void doSend(ChatMessage cm) {
        if (null == cm) {
            return;
        }
        cm.setMaster(master);
        cm.setFromuser(master);
        cm.setFromusernick(master);
        cm.setTouser(friend);
        cm.setTousernick(friendNick);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cm.setTime(df.format(new Date()));
        String clientId = CommonUtil.getStringToDate(cm.getTime()) + (int) (Math.random() * 10);
        cm.setClientId(clientId);
        cm.setUsername(friend);
        cm.setNickname(friendNick);
        cm.setType(chatType);
        cm.setMsgState("read");
        cm.setProgress("true");

        chatMsgs.add(cm);
        mAdapter.notifyDataSetChanged();
        scrollToBottom();
        if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
            new uploadAttachment().execute(cm);
        }else {
            sendBroadcast(cm);
        }

    }

    private class uploadAttachment extends AsyncTask<Object, Void, ResultData> {

        private ChatMessage cm;

        @Override
        protected ResultData doInBackground(Object... params) {
            Log.d(TAG, "doInBackground: ");
            cm = (ChatMessage) params[0];
            File file = new File(cm.getUrl());
            ResultData data = ResultManager.createSuccessData(null);
            try {
                data = FileUtils.uploadFile(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(ResultData data) {
            super.onPostExecute(data);
            if (ResultManager.isOk(data)) {
                Log.d(TAG, "doInBackground: " + (String) data.getData());
                try {
                    JSONObject object = new JSONObject((String) data.getData());
                    String mediaUrl = object.getString("ok");
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage = cm;
                    chatMessage.setUrl(mediaUrl);
                    sendBroadcast(cm);
                } catch (Exception e) {
                    e.printStackTrace();
                    unsend(cm);
                }
            } else {
                unsend(cm);
            }
        }
    }

    private void unsend(ChatMessage cm) {
        if (cm.getPrompt().equals("false")) {
            ChatMessage chatMessage = cm;
            chatMessage.setPrompt("true");
            chatMessage.setResend("false");
            chatMessage.setProgress("false");
            IMChatManager.getInstance(getActivity()).save(chatMessage);
            chatMsgs.remove(chatMsgs.size() - 1);
            chatMsgs.add(chatMessage);
            mAdapter.notifyDataSetChanged();
            scrollToBottom();
        } else {
            for (ChatMessage chat : chatMsgs) {
                if (chat.getClientId().equals(cm.getClientId())) {
                    chat.setPrompt("true");
                    chat.setResend("false");
                    chat.setProgress("false");
                    mAdapter.notifyDataSetChanged();
                    scrollToBottom();
                }
            }
        }
        ToastUtils.showShort(getActivity(), "发送失败,");
    }

    private void sendBroadcast(ChatMessage cm) {
        if (CommonUtil.isWorked(getActivity(), "com.websocketim.service.SocketService")) {
            Intent intent = new Intent(Constants.SEND_CHATMESSAGE);
            Bundle bundle = new Bundle();
            bundle.putSerializable("ChatMessage", cm);
            intent.putExtras(bundle);
            getActivity().sendBroadcast(intent);
            if (cm.getResend().equals("false")) {
                SaveChatMessage(cm);
            }
        } else {
            cm.setPrompt("true");
            cm.setProgress("false");
            chatMsgs.remove(chatMsgs.size() - 1);
            chatMsgs.add(cm);
            mAdapter.notifyDataSetChanged();
            scrollToBottom();
            SaveChatMessage(cm);
            getActivity().startService(new Intent(getActivity(), SocketService.class));
            ToastUtils.showShort(getActivity(), "未连接服务器");
        }
    }

    private void SaveChatMessage(ChatMessage cm) {
        ChatMessage msg = new ChatMessage();
        msg.setMaster(cm.getMaster());
        msg.setContent(cm.getContent());
        msg.setFromuser(cm.getFromuser());
        msg.setFromusernick(cm.getFromusernick());
        msg.setTouser(cm.getTouser());
        msg.setTousernick(cm.getTousernick());
        msg.setTime(cm.getTime());
        msg.setClientId(cm.getClientId());
        msg.setType(cm.getType());
        msg.setAvatar(cm.getAvatar());
        msg.setNickname(cm.getNickname());
        msg.setUsername(cm.getUsername());
        msg.setUrl(cm.getUrl());
        msg.setExtra(cm.getExtra());
        msg.setThumbnailUrl(cm.getThumbnailUrl());
        msg.setContentType(cm.getContentType());
        msg.setPrompt("true");
        IMChatManager.getInstance(getActivity()).save(msg);
    }

    private void stopRecordSoundUnSave() {
        voiceRecorderView.setVisibility(View.GONE);
        isRecording = false;
        if (null != mediaRecorder) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            recordStartTime = 0;
        }
    }

    public void initVoiceRecorderView() {
        voiceRecorderView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRecording && mediaRecorder != null) {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    int i = mediaRecorder.getMaxAmplitude() * 13 / 0x7FFF;
                                    voiceRecorderView.startRecorder(i);
                                } catch (Exception e) {

                                }
                            }
                        });
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        isShowing = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isShowing = false;
        if (null != mediaPlayer && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (null != mediaPlayer) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (null != chatReceiver) {
            getActivity().unregisterReceiver(chatReceiver);
        }
        if (null != newMsgReceiver) {
            getActivity().unregisterReceiver(newMsgReceiver);
        }
    }

    // 将信息状态设为已读
    private void read(ChatMessage cm) {
        if (null != cm) {
            DBHelper helper = new DBHelper(getActivity());
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("UPDATE chat_history SET msg_state='read' WHERE msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "' AND msg_content=? AND msg_time='" + cm.getTime() + "' AND msg_type='" + cm.getType() + "' AND username='" + master + "'", new Object[]{cm.getContent()});
            // 未读消息数改为0
            db.execSQL("UPDATE chat_recent SET msg_unread_count=0 WHERE username='" + master + "' " +
                            "AND msg_type='" + cm.getType() + "' " +
                            "AND msg_content=? " +
                            "AND msg_time='" + cm.getTime() + "' " +
                            "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))",
                    new Object[]{cm.getContent()});
            db.close();
        }
    }

}
