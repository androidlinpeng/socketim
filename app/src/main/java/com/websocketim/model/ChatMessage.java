package com.websocketim.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/28.
 */

public class ChatMessage implements Serializable {

    public String master = "";
    public String fromuser = "";
    public String fromusernick = "";
    public String touser = "";
    public String tousernick = "";
    public String content = "";
    private String type = "";
    private String time = "";
    private String clientId = "";
    private String avatar = "";
    private String nickname = "";
    private String username = "";
    private String url = "";
    private String contentType = "";
    private int unreadCount = 0;
    private String extra = "";
    private String thumbnailUrl = "";
    private String audioState = "unread";
    private String prompt = "false";
    private String resend = "false";
    private String progress = "false";
    private String state = "";

    public final static String CHAT_CONTENT_TYPE_TXT = "txt";
    public final static String CHAT_CONTENT_TYPE_AUDIO = "audio";
    public final static String CHAT_CONTENT_TYPE_PIC = "pic";
    public final static String CHAT_CONTENT_TYPE_VIDEO = "video";

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getFromuser() {
        return fromuser;
    }

    public void setFromuser(String fromuser) {
        this.fromuser = fromuser;
    }

    public String getFromusernick() {
        return fromusernick;
    }

    public void setFromusernick(String fromusernick) {
        this.fromusernick = fromusernick;
    }

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTousernick() {
        return tousernick;
    }

    public void setTousernick(String tousernick) {
        this.tousernick = tousernick;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentDescr() {
        if (contentType.equals(CHAT_CONTENT_TYPE_TXT)) {
            return content;
        } else if (contentType.equals(CHAT_CONTENT_TYPE_AUDIO)) {
            return "[语音]";
        } else if (contentType.equals(CHAT_CONTENT_TYPE_PIC)) {
            return "[图片]";
        } else if (contentType.equals(CHAT_CONTENT_TYPE_VIDEO)){
            return "[视频]";
        }else {
            return content;
        }
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getUnreadCount() {
        return this.unreadCount;
    }

    public void setUnreadCount(int count) {
        this.unreadCount = count;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getAudioState() {
        return audioState;
    }

    public void setAudioState(String audioState) {
        this.audioState = audioState;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResend() {
        return resend;
    }

    public void setResend(String resend) {
        this.resend = resend;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getMsgState() {
        return state;
    }

    public void setMsgState(String state) {
        this.state = state;
    }
}
