package com.websocketim.utils;

import android.content.Context;

import java.io.File;

public class MsgCache {

    public static ACache get(Context cxt) {
        return get(cxt, false);
    }

    public static ACache get(Context cxt, boolean withUser) {
        String path = cxt.getFilesDir()
                + File.separator
                + "cache"
                + File.separator
                + "ACache";
        File file = new File(path);
        return ACache.get(file);
    }
}
