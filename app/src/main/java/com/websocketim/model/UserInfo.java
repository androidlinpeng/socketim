package com.websocketim.model;

import com.websocketim.utils.CommonUtil;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/13.
 */

public class UserInfo implements Serializable {

    public String username;
    public String nickname;
    public String gender;
    public String mood="";
    public String city;
    public String head50="";
    public String head100="";
    public String head320="";

    public String initialLetter = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHead50() {
        return head50;
    }

    public void setHead50(String head50) {
        this.head50 = head50;
    }

    public String getHead100() {
        return head100;
    }

    public void setHead100(String head100) {
        this.head100 = head100;
    }

    public String getHead320() {
        return head320;
    }

    public void setHead320(String head320) {
        this.head320 = head320;
    }

    public String getInitialLetter() {
        if(initialLetter == null){
            CommonUtil.setUserInitialLetter(this);
        }
        return initialLetter;
    }

    public void setInitialLetter(String initialLetter) {
        this.initialLetter = initialLetter;
    }
}
