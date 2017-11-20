package com.websocketim;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/5/28.
 */

public class DBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "User.db";
    private final static int DB_VERSION = 1;
    private static volatile DBHelper sInstance;

    private final Context mContext;

    public DBHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    public static DBHelper getsInstance(Context context) {
        if (sInstance == null) {
            synchronized (DBHelper.class) {
                if (sInstance == null) {
                    sInstance = new DBHelper(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE im_message_center (_id INTEGER PRIMARY KEY AUTOINCREMENT,user VARCHAR,fromname VARCHAR,textContent VARCHAR)");
        db.execSQL("CREATE TABLE im_chat_list (_id INTEGER PRIMARY KEY AUTOINCREMENT,master VARCHAR,type VARCHAR,title VARCHAR,content VARCHAR,state VARCHAR)");
        createChatTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            createChatTable(db);
        }

    }

    private void createChatTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE chat_history (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username VARCHAR," + // 用户
                "msg_from VARCHAR," + // 消息发送方
                "msg_fromnick VARCHAR," + // 消息发送方昵称
                "msg_to VARCHAR," +   // 消息接收方
                "msg_tonick VARCHAR," +   // 消息接收方昵称
                "msg_content VARCHAR," + // 消息内容
                "msg_type VARCHAR," +  // 消息类型 chat 或者 groupchat
                "msg_time VARCHAR," + // 消息时间戳
                "msg_clientId VARCHAR," + // 消息客户id
                "msg_avatar VARCHAR,"+ // 消息来源的头像
                "msg_nickname VARCHAR,"+ // 消息来源的昵称
                "msg_username VARCHAR,"+ // 消息来源的用户名
                "msg_media_url VARCHAR,"+ // 多媒体消息url
                "msg_content_type VARCHAR,"+ // 消息内容类型
                "msg_media_extra VARCHAR,"+ // 多媒体消息额外信息
                "msg_media_thumbnail VARCHAR,"+ // 多媒体消息缩略图
                "msg_audio_state VARCHAR,"+ // 语音消息状态read或unread
                "msg_prompt VARCHAR,"+ // 消息发送失败标识
                "msg_state VARCHAR)");  // read或者是unread

        // 最近聊天记录
        db.execSQL("CREATE TABLE chat_recent (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username VARCHAR," + // 用户
                "msg_from VARCHAR," + // 消息发送方
                "msg_fromnick VARCHAR," + // 消息发送方昵称
                "msg_to VARCHAR," +   // 消息接收方
                "msg_tonick VARCHAR," +   // 消息接收方昵称
                "msg_content VARCHAR," + // 消息内容
                "msg_type VARCHAR," +  // 消息类型 chat 或者 groupchat
                "msg_time VARCHAR," + // 消息时间戳
                "msg_clientId VARCHAR," + // 消息客户id
                "msg_avatar VARCHAR,"+ // 消息来源的头像
                "msg_nickname VARCHAR,"+ // 消息来源的昵称
                "msg_username VARCHAR,"+ // 消息来源的用户名
                "msg_unread_count VARCHAR,"+ // 未读消息数量
                "msg_media_url VARCHAR,"+ // 多媒体消息url
                "msg_content_type VARCHAR,"+ // 消息内容类型
                "msg_media_extra VARCHAR,"+ // 多媒体消息额外信息
                "msg_media_thumbnail VARCHAR,"+ // 多媒体消息缩略图
                "msg_audio_state VARCHAR,"+ // 语音消息状态read或unread
                "msg_prompt VARCHAR,"+ // 消息发送失败标识
                "msg_state VARCHAR)");  // read或者是unread
    }

    private final static String EMOTION_DB_NAME = "emotion.db";

    public static SQLiteDatabase getEmotionDatabase(){
        try{
            String dbPath = MyApplication.getInstance().getDatabasePath(EMOTION_DB_NAME).getParent();
            File dbPathDir = new File(dbPath);
            if (!dbPathDir.exists()) {
                dbPathDir.mkdirs();
            }

            File dest = new File(dbPathDir, EMOTION_DB_NAME);
            if (!dest.exists()){
                dest.createNewFile();
                InputStream is = MyApplication.getInstance().getAssets().open(EMOTION_DB_NAME);
                int size = is.available();
                byte buf[] = new byte[size];
                is.read(buf);
                is.close();
                FileOutputStream fos = new FileOutputStream(dest);
                fos.write(buf);
                fos.close();
            }

            return SQLiteDatabase.openOrCreateDatabase(MyApplication.getInstance().getDatabasePath(EMOTION_DB_NAME), null);

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

}