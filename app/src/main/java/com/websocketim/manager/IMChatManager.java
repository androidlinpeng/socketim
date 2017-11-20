package com.websocketim.manager;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.websocketim.Constants;
import com.websocketim.DBHelper;
import com.websocketim.R;
import com.websocketim.activity.IMChatActivity;
import com.websocketim.activity.MainActivity;
import com.websocketim.activity.RedPacketRoomActivity;
import com.websocketim.model.ChatMessage;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.MsgCache;

import java.net.Socket;
import java.util.UUID;

import static com.websocketim.Constants.FRAGMENT_FRIEND;
import static com.websocketim.Constants.FRAGMENT_GROUP;


/**
 * Created by liang on 2017/5/27.
 */

public class IMChatManager {

    private static final String TAG = "IMChatManager";

    private static IMChatManager mInstance;

    private Context context;

    private LocalBroadcastManager localBroadcastManager = null;

    private Socket socket;

    private String master = "";

    public static IMChatManager getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new IMChatManager(context);
        }
        return mInstance;
    }

    private IMChatManager(Context context) {
        this.context = context;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        UserInfo userinfo = (UserInfo) MsgCache.get(context).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
        }
    }

    // 打开聊天窗口
    public static void startChat(Activity activity, String master, String avator,String type, String name, String nickname) {
        Bundle bundle = new Bundle();
        bundle.putString("master", master);
        bundle.putString("avator", avator);
        bundle.putString("chat_type", type);
        bundle.putString("chat_friend", name);
        bundle.putString("chat_friendNick", nickname);
        Intent intent = new Intent();
        intent.setClass(activity, IMChatActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    // 打开聊天窗口
    public static void RedPacketstartChat(Activity activity, String master, String avator,String type, String name, String nickname) {
        Bundle bundle = new Bundle();
        bundle.putString("master", master);
        bundle.putString("avator", avator);
        bundle.putString("chat_type", type);
        bundle.putString("chat_friend", name);
        bundle.putString("chat_friendNick", nickname);
        Intent intent = new Intent();
        intent.setClass(activity, RedPacketRoomActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    // 将消息保存到数据库
    public void save(ChatMessage cm) {

        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", cm.getMaster());
        values.put("msg_from", cm.getFromuser());
        values.put("msg_fromnick", cm.getFromusernick());
        values.put("msg_to", cm.getTouser());
        values.put("msg_tonick", cm.getTousernick());
        values.put("msg_content", cm.getContent());
        values.put("msg_type", cm.getType());
        values.put("msg_time", cm.getTime());
        values.put("msg_clientId", cm.getClientId());
        values.put("msg_avatar", cm.getAvatar());
        values.put("msg_username", cm.getUsername());
        values.put("msg_nickname", cm.getNickname());
        values.put("msg_state", cm.getMsgState());
        values.put("msg_media_url", cm.getUrl());
        values.put("msg_content_type", cm.getContentType());
        values.put("msg_media_extra", cm.getExtra());
        values.put("msg_media_thumbnail", cm.getThumbnailUrl());
        values.put("msg_audio_state", cm.getAudioState());
        values.put("msg_prompt", cm.getPrompt());

        db.insert("chat_history", null, values);

        // 将消息更新至最近聊天列表
        saveCMtoRecent(cm);
        // 发送更新通知
        Intent intent = new Intent("CHAT_RECENT_MESSAGE");
        this.localBroadcastManager.sendBroadcast(intent);

        db.close();
    }

    // 更新信息到最近聊天数据库
    public void saveCMtoRecent(ChatMessage cm) {
        ContentValues values = new ContentValues();
        values.put("username", cm.getMaster());
        if (cm.getFromuser().equals(cm.getMaster())) {
            values.put("msg_from", cm.getTouser());
            values.put("msg_fromnick", cm.getTousernick());
            values.put("msg_to", cm.getFromuser());
            values.put("msg_tonick", cm.getFromusernick());
        } else {
            values.put("msg_from", cm.getFromuser());
            values.put("msg_fromnick", cm.getFromusernick());
            values.put("msg_to", cm.getTouser());
            values.put("msg_tonick", cm.getTousernick());
            values.put("msg_avatar", cm.getAvatar());
//            values.put("msg_nickname", cm.getNickname());
//            values.put("msg_username", cm.getUsername());
        }
        values.put("msg_nickname", cm.getNickname());
        values.put("msg_username", cm.getUsername());
        values.put("msg_content", cm.getContent());
        values.put("msg_type", cm.getType());
        values.put("msg_time", cm.getTime());
        values.put("msg_clientId", cm.getClientId());
        values.put("msg_media_url", cm.getUrl());
        values.put("msg_content_type", cm.getContentType());
        values.put("msg_media_extra", cm.getExtra());
        values.put("msg_media_thumbnail", cm.getThumbnailUrl());
        values.put("msg_audio_state", cm.getAudioState());
        values.put("msg_prompt", cm.getPrompt());

        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 计算未读消息数量
        int unreadCount = 0;
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM chat_history WHERE username='" + cm.getMaster() + "' " +
                "AND msg_type='" + cm.getType() + "' " +
                "AND msg_state='unread' " +
                "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))", null);
        if (cur.moveToNext()) {
            unreadCount = cur.getInt(0);
        }
        values.put("msg_unread_count", unreadCount);
        // 将最近聊天数据更新至数据库
        cur = db.rawQuery("SELECT _id FROM chat_recent WHERE username='" + cm.getMaster() + "' " +
                "AND msg_type='" + cm.getType() + "' " +
                "AND ((msg_from='" + cm.getFromuser() + "' AND msg_to='" + cm.getTouser() + "') OR (msg_from='" + cm.getTouser() + "' AND msg_to='" + cm.getFromuser() + "'))", null);
        if (cur.moveToNext()) {
            db.update("chat_recent", values, "_id=?", new String[]{cur.getInt(0) + ""});
        } else {
            db.insert("chat_recent", null, values);
        }

        cur.close();
        db.close();
    }

    // 收到消息的处理
    public void onRevMsg(ChatMessage cm) {
        if (null != cm) {
            // 依次保存，通知，广播
            save(cm);
            notice(cm);
            sendBroadCast(cm);
        }
    }

    private static final int NOTIFY_NEW_MSG = 90;

    // 将消息提示到通知栏
    private void notice(ChatMessage cm) {
//        if (!IMActivity.isShowing && !IMChatActivity.isShowing) {
        if (!CommonUtil.isForeground(context, "com.websocketim.activity.MainActivity")
                && !CommonUtil.isForeground(context, "com.websocketim.activity.IMChatActivity")) {
            // 顶栏聊天对象名字
            // 优先取用户名对应的contact title
            // 为空去nickname
            // 再为空就取from
            String showName = cm.getFromusernick();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setTicker(showName + ":" + cm.getContentDescr());
            builder.setContentTitle("有新消息");
            builder.setContentText(showName + ":" + cm.getContentDescr());
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
            builder.setAutoCancel(true);

            Intent intent = new Intent(context, MainActivity.class);
            Bundle bundle = new Bundle();
            if (cm.getType().equals(FRAGMENT_FRIEND)) {
                bundle.putString("which_fragment", FRAGMENT_FRIEND);
            } else {
                bundle.putString("which_fragment", FRAGMENT_GROUP);
            }
            bundle.putBoolean("back_to_mainactivity", true);
            intent.putExtras(bundle);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFY_NEW_MSG, builder.build());
        }
    }

    //单条聊天消息更新
    public void updateSendSuccess(ChatMessage msg){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msg_prompt", "false");
        Cursor cur = db.rawQuery("SELECT _id FROM chat_history WHERE username='" + msg.getMaster() + "' " +
                "AND msg_type='" + msg.getType() + "' " +
                "AND msg_clientId='" + msg.getClientId() + "' " +
                "AND ((msg_from='" + msg.getFromuser() + "' AND msg_to='" + msg.getTouser() + "') OR (msg_from='" + msg.getTouser() + "' AND msg_to='" + msg.getFromuser() + "'))", null);
        if (cur.moveToNext()) {
            db.update("chat_history", values, "_id=?", new String[]{cur.getInt(0) + ""});
        }
        cur.close();
        db.close();
    }

    //语音状态更新
    public void updateAudioState(ChatMessage msg){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msg_audio_state", "read");
        Cursor cur = db.rawQuery("SELECT _id FROM chat_history WHERE username='" + msg.getMaster() + "' " +
                "AND msg_type='" + msg.getType() + "' " +
                "AND msg_clientId='" + msg.getClientId() + "' " +
                "AND ((msg_from='" + msg.getFromuser() + "' AND msg_to='" + msg.getTouser() + "') OR (msg_from='" + msg.getTouser() + "' AND msg_to='" + msg.getFromuser() + "'))", null);
        if (cur.moveToNext()) {
            db.update("chat_history", values, "_id=?", new String[]{cur.getInt(0) + ""});
        }
        cur.close();
        db.close();
    }

    // 以广播的方式将消息发送出去
    private void sendBroadCast(ChatMessage cm) {
        Intent intent = new Intent(Constants.CHAT_NEW_MESSAGE);
        intent.putExtra("msg_from", cm.getFromuser());
        intent.putExtra("msg_fromnick", cm.getFromusernick());
        intent.putExtra("msg_to", cm.getTouser());
        intent.putExtra("msg_tonick", cm.getTousernick());
        intent.putExtra("msg_content", cm.getContent());
        intent.putExtra("msg_type", cm.getType());
        intent.putExtra("msg_time", cm.getTime());
        intent.putExtra("msg_clientId", cm.getClientId());
        intent.putExtra("msg_avatar", cm.getAvatar());
        intent.putExtra("msg_nickname", cm.getNickname());
        intent.putExtra("msg_username", cm.getUsername());
        intent.putExtra("msg_media_url", cm.getUrl());
        intent.putExtra("msg_content_type", cm.getContentType());
        intent.putExtra("msg_media_extra", cm.getExtra());
        intent.putExtra("msg_media_thumbnail", cm.getThumbnailUrl());
        intent.putExtra("msg_prompt", cm.getPrompt());
        context.sendBroadcast(intent);
    }
}
