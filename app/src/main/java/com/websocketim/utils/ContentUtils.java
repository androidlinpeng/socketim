package com.websocketim.utils;

import android.util.Log;

import com.websocketim.Constants;
import com.websocketim.model.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/10/21.
 */

public class ContentUtils {

    private static final String TAG = "SocketService";

    public static final int SIZEOF_INT = Integer.SIZE / Byte.SIZE;

    public static byte[] Heartbeat(String master, double maxStreamIndex) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", 1);
            json.put("uid", Integer.parseInt(master));
            json.put("lastIndex", maxStreamIndex);
            Log.d(TAG, "Heartbeat: master "+master);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return context(json);
    }

    public static byte[] sendMessage(ChatMessage cm) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", 2);
            json.put("uid", Integer.parseInt(cm.getMaster()));
            json.put("clientId", cm.getClientId());
            json.put("chatType", 1);
            if (cm.getType().equals(Constants.FRAGMENT_FRIEND)) {
                json.put("chatType", 1);
            } else if (cm.getType().equals(Constants.FRAGMENT_GROUP)){
                json.put("chatType", 2);
            }
            if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_TXT)) {
                json.put("word", cm.getContent());
                json.put("voice", "");
                json.put("voiceDuration", "");
                json.put("video", "");
                json.put("videoDuration", "");
                json.put("picture", "");
            } else if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_PIC)) {
                json.put("word", "");
                json.put("voice", "");
                json.put("voiceDuration", "");
                json.put("video", "");
                json.put("videoDuration", "");
                json.put("picture", cm.getUrl());
            } else if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_AUDIO)) {
                json.put("word", "");
                json.put("voice", cm.getUrl());
                json.put("voiceDuration", cm.getExtra());
                json.put("video", "");
                json.put("videoDuration", "");
                json.put("picture", "");
            } else if (cm.getContentType().equals(ChatMessage.CHAT_CONTENT_TYPE_VIDEO)) {
                json.put("word", "");
                json.put("voice", "");
                json.put("voiceDuration", "");
                json.put("video", cm.getUrl());
                json.put("videoDuration", cm.getExtra());
                json.put("picture", "");
            }
            json.put("thirdId", Integer.parseInt(cm.getTouser()));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "sendMessage: e " + e.getMessage());
        }
        return context(json);
    }

    public static byte[] context(JSONObject obj) {
        String json = obj.toString();
        byte[] bytes = json.getBytes();
        byte[] head = putInt(bytes.length);
        byte[] result = new byte[bytes.length + head.length];
        for (int i = 0; i < head.length; i++) {
            result[i] = head[i];
        }
        for (int i = 0; i < bytes.length; i++) {
            result[i + head.length] = bytes[i];
        }
        return result;
    }

    public static void putInt(byte[] bb, int x, int index) {
        bb[index + 0] = (byte) (x >> 24);
        bb[index + 1] = (byte) (x >> 16);
        bb[index + 2] = (byte) (x >> 8);
        bb[index + 3] = (byte) (x >> 0);
    }

    public static byte[] putInt(int x) {
        byte[] bb = new byte[SIZEOF_INT];
        putInt(bb, x, 0);
        return bb;
    }

}
