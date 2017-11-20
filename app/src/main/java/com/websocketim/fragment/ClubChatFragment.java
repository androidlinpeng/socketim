package com.websocketim.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.websocketim.Constants;
import com.websocketim.DBHelper;
import com.websocketim.R;
import com.websocketim.activity.CaptureVideoActivity;
import com.websocketim.activity.ContactListActivity;
import com.websocketim.activity.GalleryActivity;
import com.websocketim.activity.UserInfoPreviewActivity;
import com.websocketim.activity.VideoPlayActivity;
import com.websocketim.asynchttp.ResultData;
import com.websocketim.asynchttp.ResultManager;
import com.websocketim.manager.DialogManager;
import com.websocketim.manager.IMChatManager;
import com.websocketim.manager.PlaySoundManager;
import com.websocketim.model.ChatMessage;
import com.websocketim.service.SocketService;
import com.websocketim.utils.BitmapUtils;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.FileUtils;
import com.websocketim.utils.GlideLoader;
import com.websocketim.utils.ToastUtils;
import com.websocketim.view.EmotionContainerView;
import com.websocketim.view.RelativeLayoutHasResizeListener;
import com.websocketim.view.VoiceRecorderView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/11/9.
 */

public class ClubChatFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "ClubChatFragment";

    private ListView chat_list;
    private EditText chat;
    private String chatText = "";
    //发送
    private ImageButton send;
    private TextView sendText;
    // 表情
    private ImageButton emotion = null;
    //媒体
    private ImageButton media = null;
    //录制声音音量
    private VoiceRecorderView voiceRecorderView;
    //相册
    private ImageButton pictures;
    //相机
    private ImageButton takephoto;
    //视频
    private ImageButton video;
    //语音
    private Button chatSound;
    //输入框的view
    private RelativeLayout rlChatContent;
    //录制语音的view
    private RelativeLayout rlChatSound;


    // 当前登录的用户名
    private String master = "";
    private String avator = "";
    private String nickname = "";
    // 聊天对象
    private String friend = "";
    private String friendNick = "";
    // 聊天类型 chat或者groupchat
    private String chatType = "";
    // activity是否可见
    public static boolean isShowing = false;
    // 添加表情的container
    private EmotionContainerView emotionContainer = null;
    //多媒体的view
    private LinearLayout mediaContainer = null;
    // 显示表情和多媒体的view的container
    private View extraContainer = null;
    // 添加图片
    private TextView addPic = null;
    // 是否在load历史聊天记录
    private boolean isLoadingMore = false;

    private ChatMessageAdapter mAdapter;
    private List<ChatMessage> chatMsgs = new ArrayList<ChatMessage>();
    private HashMap<String, String> emotionKV = new HashMap<String, String>();

    private String curPhotoPath = "";

    private static final int RESULT_GALLERY = 100;
    private static final int RESULT_CAMERA = 200;
    private final static int REQUEST_VIDEO = 300;

    // 播放声音
    private MediaPlayer mediaPlayer = null;

    // 发送按钮的状态
    private enum SEND_STATE {
        SOUND_STATE_IN,
        SOUND_STATE_OUT
    }

    // activity不可见的时候收到的消息
    private List<ChatMessage> chatMsgOnPause = new ArrayList<ChatMessage>();
    //新消息
    private NewMsgReceiver newMsgReceiver = null;

    //聊天
    private ChatReceiver chatReceiver = null;

    private List<String> imagList = new ArrayList<String>();

    public static Activity activity;

    private File currentVideoFile = null;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        View rootView = inflater.inflate(R.layout.fragment_club_chat, null);


        this.mediaPlayer = new MediaPlayer();
        chat_list = rootView.findViewById(R.id.chat_list);
        chat = rootView.findViewById(R.id.chat_content);
        send =  rootView.findViewById(R.id.send);
        sendText =  rootView.findViewById(R.id.sendText);
        emotion =  rootView.findViewById(R.id.emotion);
        media =  rootView.findViewById(R.id.media);
        voiceRecorderView =  rootView.findViewById(R.id.voice_recorder);
        pictures =  rootView.findViewById(R.id.pictures);
        takephoto = rootView.findViewById(R.id.takephoto);
        video =  rootView.findViewById(R.id.video);
        chatSound =  rootView.findViewById(R.id.chat_sound);
        rlChatSound =  rootView.findViewById(R.id.rl_chat_sound);
        rlChatContent = rootView.findViewById(R.id.rl_chat_content);
        emotionContainer = rootView.findViewById(R.id.emotion_container);
        mediaContainer = rootView.findViewById(R.id.media_container);
        extraContainer = rootView.findViewById(R.id.emotion_media_container);

        send.setTag(SEND_STATE.SOUND_STATE_OUT);
        send.setOnClickListener(this);
        sendText.setOnClickListener(this);
        emotion.setOnClickListener(this);
        pictures.setOnClickListener(this);
        takephoto.setOnClickListener(this);
        video.setOnClickListener(this);
        media.setOnClickListener(this);
        chatSound.setOnClickListener(this);
        emotionContainer.setEditText(chat);

        initData();

        // 输入法弹出，列表自动滚到底部
        RelativeLayoutHasResizeListener hasResizeListener = (RelativeLayoutHasResizeListener) rootView.findViewById(R.id.root_layout);
        hasResizeListener.setOnResizeListener(new RelativeLayoutHasResizeListener.OnResizeListener() {

            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                scrollToBottom();
            }
        });

        // 点击输入框收起表情或多媒体
        this.chat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                extraContainer.setVisibility(View.GONE);
                rlChatSound.setVisibility(View.GONE);
                rlChatContent.setVisibility(View.VISIBLE);
                if (!CommonUtil.isBlank(chatText)) {
                    sendText.setVisibility(View.VISIBLE);
                }
                return false;
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
        this.chat_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (send.getTag().equals(SEND_STATE.SOUND_STATE_IN)) {
                    send.setTag(SEND_STATE.SOUND_STATE_OUT);
                    send.setImageResource(R.drawable.ic_im_chat_sound_toggle_black);
                }
                CommonUtil.hideSoftInput(getActivity());
                extraContainer.setVisibility(View.GONE);
                rlChatSound.setVisibility(View.GONE);
                rlChatContent.setVisibility(View.VISIBLE);
                return false;
            }
        });
        this.chat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (null != charSequence) {
                    chatText = charSequence.toString();
                    if (charSequence.toString().equals("")) {
                        send.setImageResource(R.drawable.ic_im_chat_sound_toggle_black);
                        send.setTag(SEND_STATE.SOUND_STATE_OUT);
                        sendText.setVisibility(View.GONE);
                    } else {
                        sendText.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        this.chatSound.setOnTouchListener(new View.OnTouchListener() {

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
                        chatSound.setBackgroundResource(R.drawable.bg_im_chat_sound_ctrl);
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
                        chatSound.setBackgroundResource(R.drawable.bg_im_chat_sound);
                        break;

                    default:
                        break;
                }
                return false;
            }
        });

        this.chatReceiver = new ChatReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.SEND_CHATMESSAGE_SUCCESS);
        intentFilter.addAction(Constants.SEND_CHATMESSAGE_FAIL);
        getActivity().registerReceiver(this.chatReceiver, intentFilter);

        this.newMsgReceiver = new NewMsgReceiver();
        getActivity().registerReceiver(this.newMsgReceiver, new IntentFilter(Constants.CHAT_NEW_MESSAGE));

        return rootView;
    }

    public static void newInstance() {
        Log.d(TAG, "newInstance: ");
    }

    private MediaRecorder mediaRecorder = null;

    private boolean isRecording = false;

    private String curSoundPath = "";
    // 开始录制的时间
    private long recordStartTime = 0;

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
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    int i = mediaRecorder.getMaxAmplitude() * 13 / 0x7FFF;
//                                    voiceRecorderView.startRecorder(i);
//                                } catch (Exception e) {
//
//                                }
//                            }
//                        });
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
        if (null != this.chatReceiver) {
            getActivity().unregisterReceiver(this.chatReceiver);
        }
        if (null != this.newMsgReceiver) {
            getActivity().unregisterReceiver(this.newMsgReceiver);
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

    public void initData() {
        Bundle bundle = getArguments();
        if (null != bundle) {
            this.master = bundle.getString("master");
            this.avator = bundle.getString("avator");
            this.nickname = "我";
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
        setImageData(chatMsgs, imagList);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        int sendId = R.id.send;
        int sendTextId = R.id.sendText;
        int emotionId = R.id.emotion;
        int picturesId = R.id.pictures;
        int takephotoId = R.id.takephoto;
        int videoId = R.id.video;
        int mediaId = R.id.media;
        int chatSoundId = R.id.chat_sound;

        Intent intent = null;
        if (id == sendId) {
            if (send.getTag().equals(SEND_STATE.SOUND_STATE_OUT)) {
                CommonUtil.hideSoftInput(getActivity());
                this.extraContainer.setVisibility(View.GONE);
                this.emotionContainer.setVisibility(View.GONE);
                this.mediaContainer.setVisibility(View.GONE);
                this.rlChatContent.setVisibility(View.GONE);
                this.rlChatSound.setVisibility(View.VISIBLE);
                scrollToBottom();
                send.setTag(SEND_STATE.SOUND_STATE_IN);
                send.setImageResource(R.drawable.ic_im_chat_keyboard_toggle_black);
                sendText.setVisibility(View.GONE);
            } else if (send.getTag().equals(SEND_STATE.SOUND_STATE_IN)) {
                this.extraContainer.setVisibility(View.GONE);
                this.rlChatSound.setVisibility(View.GONE);
                this.rlChatContent.setVisibility(View.VISIBLE);
                chat.setVisibility(View.VISIBLE);
                send.setTag(SEND_STATE.SOUND_STATE_OUT);
                send.setImageResource(R.drawable.ic_im_chat_sound_toggle_black);
                if (!CommonUtil.isBlank(chatText)) {
                    sendText.setVisibility(View.VISIBLE);
                }
            }
        }else if (sendTextId == id){
            sendText();
        }else if (id == emotionId) {
            this.extraContainer.setVisibility(View.VISIBLE);
            this.emotionContainer.setVisibility(View.VISIBLE);
            this.mediaContainer.setVisibility(View.GONE);
            this.rlChatContent.setVisibility(View.VISIBLE);
            this.rlChatSound.setVisibility(View.GONE);
            CommonUtil.hideSoftInput(getActivity());
            scrollToBottom();
            chat.setVisibility(View.VISIBLE);
            if (send.getTag().equals(SEND_STATE.SOUND_STATE_IN)) {
                send.setTag(SEND_STATE.SOUND_STATE_OUT);
                send.setImageResource(R.drawable.ic_im_chat_sound_toggle_black);
            }
        } else if (mediaId == id) {
            this.extraContainer.setVisibility(View.VISIBLE);
            this.mediaContainer.setVisibility(View.VISIBLE);
            this.emotionContainer.setVisibility(View.GONE);
            this.rlChatContent.setVisibility(View.VISIBLE);
            this.rlChatSound.setVisibility(View.GONE);
            CommonUtil.hideSoftInput(getActivity());
            scrollToBottom();
            chat.setVisibility(View.VISIBLE);
            if (send.getTag().equals(SEND_STATE.SOUND_STATE_IN)) {
                send.setTag(SEND_STATE.SOUND_STATE_OUT);
                send.setImageResource(R.drawable.ic_im_chat_sound_toggle_black);
            }
        } else if (picturesId == id) {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, RESULT_GALLERY);
        } else if (takephotoId == id) {
            // 设置照片文件路径
            String fileName = "_" + System.currentTimeMillis() + "_PIC.jpg";
            File file = FileUtils.createAttachmentFile(master,fileName);
            if (null != file && file.exists()) {
                curPhotoPath = file.getPath();
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //系统7.0打开相机权限处理
                if (Build.VERSION.SDK_INT >= 24) {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                    Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } else {
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                }
                startActivityForResult(intent, RESULT_CAMERA);
            }
        } else if (videoId == id) {
            // 设置照片文件路径
            String fileName = "_" + System.currentTimeMillis() + "_VODEO.3gp";
            File videoFile = FileUtils.createAttachmentFile(master,fileName);
            currentVideoFile = videoFile;

            intent = new Intent(getActivity(), CaptureVideoActivity.class);
            intent.putExtra("videoFile", videoFile.getPath());
            startActivityForResult(intent, REQUEST_VIDEO);

        }

    }

    @Override
    public void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (arg1 == RESULT_OK) {
            String path = "";
            switch (arg0) {
                case RESULT_GALLERY:
                    if (null != arg2 && null != arg2.getData()) {
                        Uri uri = arg2.getData();
                        uri = CommonUtil.getPictureUri(arg2, getActivity());
                        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
                        Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        path = cursor.getString(columnIndex);
                        cursor.close();
                    }
                    doSendPic(path);
                    break;
                case RESULT_CAMERA:
                    path = curPhotoPath;
                    doSendPic(path);
                    break;
                case REQUEST_VIDEO:
                    if (currentVideoFile.exists()) {
                        if (null != arg2) {
                            String videoTime = arg2.getStringExtra("videoTime");
                            File videoPath = currentVideoFile;
                            doSendVideo(videoPath, videoTime);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public void doSendVideo(File videoPath, String videoTime) {
        if (!CommonUtil.isBlank(videoPath)) {
            File thumbnail_file = FileUtils.createVideoThumbnailFile(master,videoPath);
            ChatMessage cm = new ChatMessage();
            cm.setContent(videoPath.getName());
            cm.setUrl(videoPath.getPath());
            cm.setExtra(videoTime);
            cm.setThumbnailUrl(thumbnail_file.getPath());
            cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_VIDEO);
            doSend(cm);
        }
    }

    // 发送图片
    private void doSendPic(String picPath) {
        if (!CommonUtil.isBlank(picPath)) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picPath, options);
            options.inSampleSize = BitmapUtils.calculateInSampleSize(options, 480, 800);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);

            try {
                String fileName = "_" + System.currentTimeMillis() + "_480x800.jpg";
                File file = FileUtils.createAttachmentFile(master,fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                fos.flush();
                fos.close();
                bitmap.recycle();
                picPath = file.getPath();
            } catch (Exception e) {
                e.printStackTrace();
            }

            File file = new File(picPath);
            if (file.exists()) {
                ChatMessage cm = new ChatMessage();
                cm.setContent(file.getName());
                cm.setUrl(file.getPath());
                cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_PIC);
                doSend(cm);
            }
        }
    }

    private void sendText() {
        // 构造msg
        String content = chat.getText().toString();
        content = content.trim();
        // 空消息不发送
        if (CommonUtil.isBlank(content)) {
            return;
        }
        ChatMessage cm = new ChatMessage();
        cm.setContent(content);
        cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_TXT);
        // 发送消息
        doSend(cm);
        // 清空输入框
        chat.setText(null);
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
        cm.setUsername(master);
        cm.setNickname(nickname);
        cm.setType(chatType);
        cm.setMsgState("read");
        cm.setProgress("true");

        chatMsgs.add(cm);
        mAdapter.notifyDataSetChanged();
        scrollToBottom();
        
        if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
            imagList.add(0, cm.getUrl());
        }
        if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC) || cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
            new uploadAttachment().execute(cm);
        } else if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
            new uploadAttachment().execute(cm);
        } else {
            sendBroadcast(cm);
        }

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

    private final static int UPLOAD_FILES_SUCCESS = 100;
    private final static int UPLOAD_FILES_FAIL = 200;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ChatMessage cm = (ChatMessage) msg.obj;
            switch (msg.what) {
                case UPLOAD_FILES_SUCCESS:
                    sendBroadcast(cm);
                    break;
                case UPLOAD_FILES_FAIL:
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
                        if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                            imagList.add(0, chatMessage.getUrl());
                        }
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
                    break;
            }
        }
    };

    private class uploadAttachment extends AsyncTask<Object, Void, ResultData> {

        private ChatMessage cm;

        @Override
        protected ResultData doInBackground(Object... params) {
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
            if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                imagList.add(0, chatMessage.getUrl());
            }
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

    //适配器
    private class ChatMessageAdapter extends BaseAdapter {

        private final static int VIEW_TYPE_COUNT = 2;

        private final static int VIEW_TYPE_SEND = 1;
        private final static int VIEW_TYPE_REV = 0;

        private List<ChatMessage> list;
        private PlaySoundManager playSoundManager = null;
        private String curPlayUrl = "";
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
                int rev_layoutId = R.layout.row_im_chat_list_rev_item;
                int send_layoutId = R.layout.row_im_chat_list_send_item;
                int avatarId = R.id.avatar;
                int nicknameId = R.id.nickname;
                int timeId = R.id.time;
                int contentId = R.id.content;
                int sound_timeId = R.id.sound_time;
                int sound_wave_showId = R.id.sound_wave_show;
                int attach_imgId = R.id.attach_img;
                int attach_videoId = R.id.attach_video;
                int video_timeId = R.id.video_time;
                int promptId = R.id.prompt;
                int prompt_imgId = R.id.prompt_img;
                int progressId = R.id.progress;
                int progress_imgId = R.id.progress_img;
                int containerId = R.id.container;
                int text_containerId = R.id.text_container;
                int attach_img_containerId = R.id.attach_img_container;
                int attach_video_containerId = R.id.attach_video_container;
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
                        holderRev.attachImg = convertView.findViewById(attach_imgId);
                        holderRev.attachVideo = convertView.findViewById(attach_videoId);
                        holderRev.container = convertView.findViewById(containerId);
                        holderRev.textContainer = convertView.findViewById(text_containerId);
                        holderRev.attachImgContainer = convertView.findViewById(attach_img_containerId);
                        holderRev.attachVideoContainer = convertView.findViewById(attach_video_containerId);
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
                        holderSend.attachImg = convertView.findViewById(attach_imgId);
                        holderSend.attachVideo = convertView.findViewById(attach_videoId);
                        holderSend.videoTime = convertView.findViewById(video_timeId);
                        holderSend.prompt = convertView.findViewById(promptId);
                        holderSend.prompt_img = convertView.findViewById(prompt_imgId);
                        holderSend.progress = convertView.findViewById(progressId);
                        holderSend.progress_img = convertView.findViewById(progress_imgId);
                        holderSend.container = convertView.findViewById(containerId);
                        holderSend.textContainer = convertView.findViewById(text_containerId);
                        holderSend.attachImgContainer = convertView.findViewById(attach_img_containerId);
                        holderSend.attachVideoContainer = convertView.findViewById(attach_video_containerId);
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
                    if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                        holderRev.attachImgContainer.setVisibility(View.GONE);
                        holderRev.attachVideoContainer.setVisibility(View.GONE);
                        holderRev.textContainer.setVisibility(View.VISIBLE);
                        holderRev.content.setOnClickListener(new View.OnClickListener() {

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
                            AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.bg_im_chat_sound_wave_on_text_left);
                            holderRev.soundWave.setBackgroundDrawable(animationDrawable);
                            animationDrawable.start();
                        } else {
                            holderRev.soundWave.setBackgroundResource(R.drawable.ic_im_chat_sound_wave_on_text_left_3);
                        }
                        contentContainer(holderRev.content, msg, position);
                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                        holderRev.textContainer.setVisibility(View.GONE);
                        holderRev.attachVideoContainer.setVisibility(View.GONE);
                        holderRev.attachImgContainer.setVisibility(View.VISIBLE);

                        final String url = msg.getUrl();
                        holderRev.attachImg.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                sendImageData(url, imagList, position);
                            }
                        });

                        GlideLoader.LoderImage(getActivity(),url,holderRev.attachImg);

                        contentContainer(holderRev.attachImg, msg, position);
                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                        holderRev.textContainer.setVisibility(View.GONE);
                        holderRev.attachImgContainer.setVisibility(View.GONE);
                        holderRev.attachVideoContainer.setVisibility(View.VISIBLE);
                        final String videoUrl = msg.getUrl();
                        holderRev.attachVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                                intent.putExtra("attachVideo", videoUrl);
                                intent.putExtra("master", master);
                                startActivity(intent);
                            }
                        });
                        String url = msg.getThumbnailUrl();
                        GlideLoader.LoderImage(getActivity(),url,holderRev.attachVideo);
                        contentContainer(holderRev.attachVideo, msg, position);
                    } else {
                        holderRev.attachImgContainer.setVisibility(View.GONE);
                        holderRev.attachVideoContainer.setVisibility(View.GONE);
                        holderRev.textContainer.setVisibility(View.VISIBLE);
                        holderRev.content.setOnClickListener(null);
                        holderRev.soundWave.setVisibility(View.GONE);
                        holderRev.soundTime.setVisibility(View.GONE);
                        holderRev.soundTime.setText("");

                        contentContainer(holderRev.content, msg, position);
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
                    GlideLoader.LoderAvatar(getActivity(),msg.getAvatar(),holderRev.avatar);
                    break;
                case VIEW_TYPE_SEND:
                    int ic_exclamation_markId = R.drawable.ic_exclamation_mark;
                    int bg_transparentId = R.drawable.bg_transparent;
                    if (!CommonUtil.isBlank(msg.getPrompt()) && msg.getPrompt().equals("true")) {
                        if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                            holderSend.prompt.setImageResource(bg_transparentId);
                            holderSend.prompt_img.setImageResource(ic_exclamation_markId);
                            holderSend.prompt_img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    DialogManager.showDialog(getActivity(), "\n重发给消息？", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            msg.setPrompt("false");
                                            msg.setResend("true");
                                            msg.setProgress("true");
                                            notifyDataSetChanged();
                                            if (msg.getUrl().startsWith("http")) {
                                                sendBroadcast(msg);
                                            } else {
                                                new uploadAttachment().execute(msg);
                                            }
                                        }
                                    }, null, null);
                                }
                            });
                        } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                            holderSend.prompt_img.setImageResource(bg_transparentId);
                            holderSend.prompt.setImageResource(ic_exclamation_markId);
                            holderSend.prompt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    DialogManager.showDialog(getActivity(), "\n重发给消息？", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            msg.setPrompt("false");
                                            msg.setResend("true");
                                            msg.setProgress("true");
                                            notifyDataSetChanged();
                                            if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                                                if (msg.getUrl().startsWith("http")) {
                                                    sendBroadcast(msg);
                                                } else {
                                                    new uploadAttachment().execute(msg);
                                                }
                                            } else {
                                                sendBroadcast(msg);
                                            }
                                        }
                                    }, null, null);
                                }
                            });
                        }
                    } else {
                        holderSend.prompt.setImageResource(bg_transparentId);
                        holderSend.prompt_img.setImageResource(bg_transparentId);
                    }

                    Animation antv = AnimationUtils.loadAnimation(getActivity(),R.anim.loading_progressbar);
                    Animation aniv = AnimationUtils.loadAnimation(getActivity(),R.anim.loading_progressbar);
                    LinearInterpolator lin = new LinearInterpolator();
                    antv.setInterpolator(lin);
                    antv.setRepeatCount(-1);
                    aniv.setInterpolator(lin);
                    aniv.setRepeatCount(-1);
                    if (!CommonUtil.isBlank(msg.getProgress()) && msg.getProgress().equals("true")) {
                        holderSend.progress.setBackgroundResource(R.drawable.ic_search_loading);
                        holderSend.progress_img.setBackgroundResource(R.drawable.ic_search_loading);
                        holderSend.progress.startAnimation(antv);
                        holderSend.progress_img.startAnimation(aniv);
                        if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                            antv.cancel();
                            aniv.startNow();
                            holderSend.progress.setVisibility(View.GONE);
                            holderSend.progress_img.setVisibility(View.VISIBLE);
                        } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT) || msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                            antv.startNow();
                            aniv.cancel();
                            holderSend.progress.setVisibility(View.VISIBLE);
                            holderSend.progress_img.setVisibility(View.GONE);
                        }
                    } else {
                        holderSend.progress.clearAnimation();
                        holderSend.progress_img.clearAnimation();
                        holderSend.progress.setVisibility(View.GONE);
                        holderSend.progress_img.setVisibility(View.GONE);
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
                    if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                        holderSend.attachImgContainer.setVisibility(View.GONE);
                        holderSend.attachVideoContainer.setVisibility(View.GONE);
                        holderSend.textContainer.setVisibility(View.VISIBLE);
                        holderSend.content.setOnClickListener(new View.OnClickListener() {

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

                        contentContainer(holderSend.content, msg, position);
                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                        holderSend.textContainer.setVisibility(View.GONE);
                        holderSend.attachVideoContainer.setVisibility(View.GONE);
                        holderSend.attachImgContainer.setVisibility(View.VISIBLE);

                        final String url = msg.getUrl();
                        holderSend.attachImg.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                sendImageData(url, imagList, position);
                            }
                        });
                        GlideLoader.LoderImage(getActivity(),url,holderSend.attachImg);
                        contentContainer(holderSend.attachImg, msg, position);
                    } else if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                        holderSend.textContainer.setVisibility(View.GONE);
                        holderSend.attachImgContainer.setVisibility(View.GONE);
                        holderSend.attachVideoContainer.setVisibility(View.VISIBLE);
                        holderSend.videoTime.setText(msg.getExtra());
                        final String videoUrl = msg.getUrl();
                        holderSend.attachVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
                                intent.putExtra("attachVideo", videoUrl);
                                intent.putExtra("master", master);
                                startActivity(intent);
                            }
                        });
                        String url = msg.getThumbnailUrl();
                        GlideLoader.LoderImage(getActivity(),url,holderSend.attachVideo);
                        contentContainer(holderSend.attachVideo, msg, position);
                    } else {
                        holderSend.textContainer.setVisibility(View.VISIBLE);
                        holderSend.attachImgContainer.setVisibility(View.GONE);
                        holderSend.attachVideoContainer.setVisibility(View.GONE);
                        holderSend.content.setOnClickListener(null);
                        holderSend.soundWave.setVisibility(View.GONE);
                        holderSend.soundTime.setVisibility(View.GONE);
                        holderSend.soundTime.setText("");
                        contentContainer(holderSend.content, msg, position);
                    }
                    GlideLoader.LoderAvatar(getActivity(),avator,holderSend.avatar);
                    break;
                default:
                    break;
            }
            return convertView;
        }

        private void contentContainer(View view, final ChatMessage msg, final int position) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    contentManager(msg, position);
                    return false;
                }
            });

        }

        private void contentManager(final ChatMessage msg, final int position) {
            final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
            dialog.show();
            Window window = dialog.getWindow();
            window.setContentView(R.layout.view_content_dialog_alert);
            LinearLayout[] linearLayouts = new LinearLayout[]{
                    window.findViewById(R.id.ll_content1),
                    window.findViewById(R.id.ll_content2),
                    window.findViewById(R.id.ll_content3),
                    window.findViewById(R.id.ll_content4),
                    window.findViewById(R.id.ll_content5)
            };
            if (!msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT)) {
                linearLayouts[0].setVisibility(View.GONE);
            }
            for (int i = 0; i < linearLayouts.length; i++) {
                final int finalI = i;
                linearLayouts[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        if (finalI == 0) {
                            CommonUtil.contentClipboard(getActivity(), msg);
                        } else if (finalI == 1) {
                            Intent intent = new Intent(getActivity(), ContactListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("ChatMessage", msg);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else if (finalI == 2) {
                            if (list.size() - 1 == position) {
                                delete(msg, list, true);
                            } else {
                                delete(msg, list, false);
                            }
                            list.remove(msg);
                            notifyDataSetChanged();
                        } else if (finalI == 3) {

                        } else if (finalI == 4) {

                        }
                    }
                });
            }
        }

        private void sendImageData(String url, List<String> imagList, int position) {
            ArrayList mlist = new ArrayList();
            mlist.add(imagList);
            Bundle bundle = new Bundle();
            for (int i = 0; i < imagList.size(); i++) {
                if (url.equals(imagList.get(i))) {
                    bundle.putInt("index", i);
                }
            }
            bundle.putParcelableArrayList("data", mlist);
            openActivity(GalleryActivity.class, bundle);
        }

        private void openActivity(Class<?> pClass, Bundle pBundle) {
            Intent intent = new Intent(getActivity(), pClass);
            if (pBundle != null) {
                intent.putExtras(pBundle);
            }
            startActivity(intent);
        }
    }

    private void setImageData(List<ChatMessage> chatMsgs, List<String> imagList) {
        List<String> list = new ArrayList<String>();
        for (ChatMessage msg : chatMsgs) {
            if (msg.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                list.add(msg.getUrl());
            }
        }
        if (list.size() > 0) {
            imagList.clear();
            for (int i = list.size() - 1; i >= 0; i--) {
                imagList.add(list.get(i));
            }
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
        ImageView attachImg;
        ImageView attachVideo;
        TextView videoTime;
        View container, textContainer, attachImgContainer, attachVideoContainer;
    }

    static class ViewHolderSend {
        ImageView avatar;
        TextView nickname;
        TextView time;
        TextView content;
        TextView soundTime;
        ImageView soundWave;
        ImageView attachImg;
        ImageView attachVideo;
        TextView videoTime;
        ImageView prompt;
        ImageView prompt_img;
        ImageView progress;
        ImageView progress_img;
        View container, textContainer, attachImgContainer, attachVideoContainer;
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
//
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
//
//        return file;
//    }

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
            if (isUsable()) {
                List<ChatMessage> more = result;
                if (more.size() > 0) {
                    chatMsgs.addAll(0, more);
                    mAdapter.notifyDataSetChanged();
                    chat_list.setSelection(more.size());
                    setImageData(chatMsgs, imagList);

                }
            }
        }
    }

    // 新消息广播接收器
    private class NewMsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent && intent.getAction().equals(Constants.CHAT_NEW_MESSAGE)) {
                Log.d(TAG, "onReceive: "+intent.getAction());

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
                        if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                            imagList.add(0, cm.getUrl());
                        }
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

    private void delete(ChatMessage cm, List<ChatMessage> list, boolean end) {
        if (null != cm) {
            DBHelper helper = new DBHelper(getActivity());
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM chat_history WHERE username='" + master + "' " +
                    "AND msg_type='" + cm.getType() + "' " +
                    "AND msg_content='" + cm.getContent() + "' " +
                    "AND msg_clientId='" + cm.getClientId() + "' " +
                    "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))");
            //更新最近聊天
            Log.d(TAG, "delete: " + list.size());
            if (end && list.size() >= 2) {
                ChatMessage msg = list.get(list.size() - 2);
                Log.d(TAG, "delete: " + msg.getContent());
                ContentValues values = new ContentValues();
                values.put("msg_content", msg.getContent());
                values.put("msg_type", msg.getType());
                values.put("msg_time", msg.getTime());
                values.put("msg_clientId", msg.getClientId());
                values.put("msg_media_url", msg.getUrl());
                values.put("msg_content_type", msg.getContentType());
                values.put("msg_media_extra", msg.getExtra());
                values.put("msg_media_thumbnail", msg.getThumbnailUrl());
                values.put("msg_prompt", msg.getPrompt());
                Cursor cur = db.rawQuery("SELECT _id FROM chat_recent WHERE username='" + cm.getMaster() + "' " +
                        "AND msg_type='" + cm.getType() + "' " +
                        "AND msg_clientId='" + cm.getClientId() + "' " +
                        "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))", null);
                if (cur.moveToNext()) {
                    db.update("chat_recent", values, "_id=?", new String[]{cur.getInt(0) + ""});
                }
                cur.close();
            } else if (list.size() == 1) {
                db.execSQL("DELETE FROM chat_recent WHERE username='" + master + "' " +
                                "AND msg_type='" + cm.getType() + "' " +
                                "AND msg_content=? " +
                                "AND msg_time='" + cm.getTime() + "' " +
                                "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))",
                        new Object[]{cm.getContent()});
            }
            db.close();
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
