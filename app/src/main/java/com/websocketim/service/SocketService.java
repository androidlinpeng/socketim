package com.websocketim.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.websocketim.Constants;
import com.websocketim.manager.IMChatManager;
import com.websocketim.model.ChatMessage;
import com.websocketim.model.UserInfo;
import com.websocketim.socket.SocketThread;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.ContentUtils;
import com.websocketim.utils.FileUtils;
import com.websocketim.utils.MsgCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;


public class SocketService extends Service {

    private static final String TAG = "SocketService";

    private String master;

    private SocketThread socket, ws;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;

    private DataInputStream wdis = null;
    private DataOutputStream wdos = null;
    //    private String reMsg = null, wreMsg = null;
    private Thread thread = null, threadwait = null;
    private Boolean isContect = false, iswContect = false;

    //主机IP地址
    private static String HOST = "182.92.191.75";
    ///端口号
    public static final String PORT = "11111";
    //心跳检测时间
    private static final long HEART_BEAT_RATE = 3 * 1000;

    public static final int CONNECT_SERVIE_ACTION = 100;
    //心跳广播
    public static final int HEART_BEAT_ACTION = 200;

    public SocketReceiver socketReceiver;

    private ChatMessage chatMessage;

    private long sendTime = 0L;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_SERVIE_ACTION:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }).start();
                    break;
                case HEART_BEAT_ACTION:

                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        socketReceiver = new SocketReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SEND_CHATMESSAGE);
        registerReceiver(socketReceiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(1, notification);
        startService(new Intent(this, FakeService.class));

        if (null != socket) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    socket.AllClose();
                }
            }).start();
        }

        UserInfo userinfo = (UserInfo) MsgCache.get(getApplication()).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
        return START_STICKY;
    }

    private void connect() {
        mHandler.removeCallbacks(heartBeatRunnable);
        socket = SocketThread.getInstance();
        if (socket.SocketStart(HOST, PORT, master)) {
            if (socket.isConnected()) {
                if (sendMsg(1, new ChatMessage())) {
                    thread = new Thread(null, doThread, "Message");
                    thread.start();
                    mHandler.removeCallbacks(heartBeatRunnable);
                    mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
                    isContect = true;
                }
            }
        } else {
            Log.d(TAG, "connect: 重新连接服务器 ");
            handler.sendEmptyMessageDelayed(CONNECT_SERVIE_ACTION, HEART_BEAT_RATE);
        }
    }

    // 发送心跳包
    private Handler mHandler = new Handler();

    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            HeartBeatThread thread = new HeartBeatThread();
            thread.start();
        }
    };

    public class HeartBeatThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (sendMsg(1, new ChatMessage())) {
                mHandler.removeCallbacks(heartBeatRunnable);
                mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
                Log.d(TAG, "run: 发送心跳成功");
            } else {
                mHandler.removeCallbacks(heartBeatRunnable);
                handler.sendEmptyMessageDelayed(CONNECT_SERVIE_ACTION, HEART_BEAT_RATE);
                Log.d(TAG, "run: 发送心跳失败");
            }
        }
    }

    private boolean sendMsg(int n, ChatMessage cm) {
        if (socket == null) {
            return false;
        }
        try {
            if (socket.isConnected()) {
                dis = socket.getDIS();
                dos = socket.getDOS();
                if (n == 1) {
                    Log.d(TAG, "sendMsg: 心跳");
                    try {
                        dos.write(ContentUtils.Heartbeat(master, parseDouble(CommonUtil.getMaxStreamIndex(this, master))));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "sendMsg: e " + e.getMessage());
                        return false;
                    }
                } else if (n == 2) {
                    Log.d(TAG, "sendMsg: 发消息");
                    try {
                        dos.write(ContentUtils.sendMessage(cm));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "sendMsg: e " + e.getMessage());
                        return false;
                    }
                }
                isContect = true;
            } else {
                Log.d(TAG, "sendMsg: ");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "sendMsg: e " + e.getMessage());
            return false;
        }
        return true;
    }

    private Runnable doThread = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: running! ");
            ReceiveMsg();
        }
    };

    private void ReceiveMsg() {
        String reMsg = null;
        byte[] buffer = new byte[1024];
        int ping = 0;
        try {
            // 获取输入流，用来接收数据
            DataInputStream stream = new DataInputStream(socket.getDIS());
            while (true) {
                // 将要接收的数据的长度
                final int dataLen = stream.readInt();
                // 缓冲区尺寸验证
                if (buffer.length < dataLen) {
                    buffer = new byte[dataLen];
                }
                // 从第0字节开始接收
                int offset = 0;

                // 接收数据
                while (offset < dataLen) {
                    int len = stream.read(buffer, offset, dataLen - offset);
                    if (len == -1) break;
                    offset += len;
                }
                // 转化为json格式，
                String command = new String(buffer, 0, offset, "UTF-8");
                try {
                    Log.d(TAG, "ReceiveMsg: command " + command);
                    JSONObject obj = new JSONObject(command);
                    int type = obj.getInt("type");
                    boolean success = obj.getBoolean("success");
                    if (type == 1) {
                        if (success) {
                            Log.d(TAG, "ReceiveMsg: 心跳回执成功\n ");
                            ping = 0;
                        } else {
                            ping += ping;
                            Log.d(TAG, "ReceiveMsg: 心跳回执失败 ： " + "\n ping " + ping);
                        }
                        if (ping < 3) {
//                            mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
                        } else {
                            handler.sendEmptyMessageDelayed(CONNECT_SERVIE_ACTION, HEART_BEAT_RATE);
                        }
                    } else if (type == 2) {
                        if (success) {
                            Log.d(TAG, "ReceiveMsg: 发送消息回执成功\n ");
                            IMChatManager.getInstance(getApplicationContext()).updateSendSuccess(chatMessage);
                            sendBroadcast(chatMessage, Constants.SEND_CHATMESSAGE_SUCCESS);
                        } else {
                            Log.d(TAG, "ReceiveMsg: 发送消息回执失败 ： ");
                            sendBroadcast(chatMessage, Constants.SEND_CHATMESSAGE_FAIL);
                        }
                    } else if (type == 5) {
                        if (success) {
                            Log.d(TAG, "ReceiveMsg: 新消息接受成功\n ");
                            JSONObject result = obj.getJSONObject("result");
                            ConnectResponse(result);
                        } else {
                            Log.d(TAG, "ReceiveMsg: 新消息接受失败 ： ");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "ReceiveMsg: e json " + e.getMessage());
                    handler.sendEmptyMessageDelayed(CONNECT_SERVIE_ACTION, HEART_BEAT_RATE);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            handler.sendEmptyMessageDelayed(CONNECT_SERVIE_ACTION, HEART_BEAT_RATE);
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendEmptyMessageDelayed(CONNECT_SERVIE_ACTION, HEART_BEAT_RATE);
        }
    }

    private void ConnectResponse(JSONObject result) {
        String maxStreamIndex = result.optString("maxStreamIndex");
        if (!maxStreamIndex.equals(CommonUtil.getMaxStreamIndex(this, master))) {
            CommonUtil.setMaxStreamIndex(this, master, "" + maxStreamIndex);
            try {
                JSONArray array = result.getJSONArray("streams");
                for (int i = 0; i < array.length(); i++) {
                    List<ChatMessage> revMsgList = new ArrayList<ChatMessage>();
                    List<ChatMessage> sendMsgList = new ArrayList<ChatMessage>();
                    JSONObject obj = array.getJSONObject(i);
                    String uid = obj.getString("uid");
                    String type = obj.getString("type");
                    String thirdId = obj.getString("thirdId");
                    String thirdNick = obj.getString("thirdNick");
                    String maxIndex = obj.getString("maxIndex");
                    JSONArray data = obj.getJSONArray("data");
                    for (int j = 0; j < data.length(); j++) {
                        JSONObject content = data.getJSONObject(j);
                        String id = content.getString("id");
                        String authorId = content.getString("authorId");
                        String authorNick = content.getString("authorNick");
                        String clientId = content.getString("clientId");
                        String msgType = content.getString("type");
                        String time = content.getString("time");
                        String index = content.getString("index");
                        JSONObject messageChat = content.getJSONObject("messageChat");

                        ChatMessage cm = new ChatMessage();
                        cm.setMaster(uid);
                        cm.setTime(CommonUtil.getDateToString(time));
                        cm.setClientId(clientId);
                        if (!CommonUtil.isBlank(messageChat.getString("word"))) {
                            String word = messageChat.getString("word");
                            cm.setContent(word);
                            cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_TXT);
                        } else if (!CommonUtil.isBlank(messageChat.getString("picture"))) {
                            String picture = messageChat.getString("picture");
                            cm.setUrl(picture);
                            cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_PIC);
                        } else if (!CommonUtil.isBlank(messageChat.getString("voice"))) {
                            String voice = messageChat.getString("voice");
                            String voiceDuration = messageChat.getString("voiceDuration");
                            cm.setUrl(voice);
                            cm.setExtra(voiceDuration);
                            cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_AUDIO);
                        } else if (!CommonUtil.isBlank(messageChat.getString("video"))) {
                            String video = messageChat.getString("video");
                            String videoDuration = messageChat.getString("videoDuration");
                            String fileName = "_" + System.currentTimeMillis() + "_480x800.jpg";
                            File compressFile = FileUtils.createAttachmentFile(master, fileName);
                            FileUtils.createVideoThumbnail(video, compressFile, 480, 800);
                            cm.setThumbnailUrl(compressFile.getPath());
                            cm.setUrl(video);
                            cm.setExtra(videoDuration);
                            cm.setContentType(ChatMessage.CHAT_CONTENT_TYPE_VIDEO);
                        }
                        if (type.equals("1")) {
                            cm.setType(Constants.FRAGMENT_FRIEND);
                        } else if (type.equals("2")) {
                            cm.setType(Constants.FRAGMENT_GROUP);
                        }
                        cm.setAvatar("https://img6.bdstatic.com/img/image/smallpic/weijuchiluntu.jpg");

                        if (!uid.equals(authorId)) {
                            cm.setFromuser(thirdId);
                            cm.setFromusernick(thirdNick);
                            cm.setTouser(uid);
                            cm.setTousernick(uid);
                            cm.setUsername(authorId);
                            cm.setNickname(authorNick);
                            cm.setMsgState("unread");
//                            revMsgList.add(cm);
                            boolean msgId = false;
                            for (ChatMessage msg : revMsgList) {
                                if (msg.getClientId().equals(cm.getClientId())) {
                                    msgId = true;
                                }
                            }
                            if (!msgId) {
                                revMsgList.add(cm);
                            }
                        } else {
                            cm.setFromuser(uid);
                            cm.setFromusernick(uid);
                            cm.setTouser(thirdId);
                            cm.setTousernick(thirdNick);
                            cm.setUsername(authorId);
                            cm.setNickname(authorNick);
                            cm.setMsgState("read");
                            cm.setPrompt("false");
                            sendMsgList.add(cm);
                        }
                    }
                    if (revMsgList.size() > 0) {
                        for (int k = revMsgList.size() - 1; k >= 0; k--) {
                            IMChatManager.getInstance(getApplicationContext()).onRevMsg(revMsgList.get(k));
                        }
                    }
                    if (sendMsgList.size() > 0) {
                        for (int k = sendMsgList.size() - 1; k >= 0; k--) {
                            IMChatManager.getInstance(getApplicationContext()).updateSendSuccess(sendMsgList.get(k));
                            sendBroadcast(sendMsgList.get(k), Constants.SEND_CHATMESSAGE_SUCCESS);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBroadcast(ChatMessage cm, String type) {
        Intent intent = new Intent(type);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ChatMessage", cm);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    private class SocketReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                String action = intent.getAction();
                Log.d(TAG, "onReceive: action " + action);
                if (action.equals(Constants.SEND_CHATMESSAGE)) {
                    Bundle bundle = intent.getExtras();
                    if (null != bundle) {
                        chatMessage = (ChatMessage) bundle.getSerializable("ChatMessage");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (sendMsg(2, chatMessage)) {
                                    Log.d(TAG, "run: 发送消息成功");
                                } else {
                                    sendBroadcast(chatMessage, Constants.SEND_CHATMESSAGE_FAIL);
                                    mHandler.removeCallbacks(heartBeatRunnable);
                                    handler.sendEmptyMessageDelayed(CONNECT_SERVIE_ACTION, HEART_BEAT_RATE);
                                    Log.d(TAG, "run: 发送消息失败");
                                }
                            }
                        }).start();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (null != socketReceiver) {
            unregisterReceiver(socketReceiver);
        }
        startService(new Intent(this, SocketService.class));
    }

}
