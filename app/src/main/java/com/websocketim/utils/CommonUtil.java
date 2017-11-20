package com.websocketim.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ecity.android.tinypinyin.Pinyin;
import com.websocketim.model.ChatMessage;
import com.websocketim.model.UserInfo;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    private static final String TAG = "CommonUtil";

    public static void contentClipboard(Context context,ChatMessage msg){
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", msg.getContent());
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        ToastUtils.showShort(context,"已复制");
    }

    private static final String Index = "MaxStreamIndex";
    private static final String CountDown = "CountDown";

    //判断service是否开启
    public static boolean isWorked(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(300);
        for (int i = 0; i < runningService.size(); i++) {
            Log.d(TAG, "isWorked: className "+runningService.get(i).service.getClassName().toString()+"  "+className);
            if (runningService.get(i).service.getClassName().toString()
                    .equals(className)) {
                return true;
            }
        }
        return false;
    }

    //判断某个Activity 界面是否在前台
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getStringToDate(String time) {
        String timeStamp = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d;
        try{
            d = sdf.parse(time);
            long l = d.getTime();
            timeStamp = String.valueOf(l);
        } catch(ParseException e){
            e.printStackTrace();
        }
        return timeStamp;
    }

    public static String getDateToString(String time) {
        long lcc = Long.valueOf(time);
        Date d = new Date(lcc);
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdr.format(d);
    }

    public static String getCountDown(Context context, String phone) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CountDown, Context.MODE_PRIVATE);
        return sharedPreferences.getString(phone, "60");
    }

    public static void setCountDown(Context context, String phone, String time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(CountDown, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(phone, time);
        editor.commit();
    }

    public static String getMaxStreamIndex(Context context, String master) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Index, Context.MODE_PRIVATE);
        return sharedPreferences.getString(master, "0");
    }

    public static void setMaxStreamIndex(Context context, String master, String maxStreamIndex) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Index, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(master, maxStreamIndex);
        editor.commit();
    }

    public static void setUserInitialLetter(UserInfo user) {
        final String DefaultLetter = "#";
        String letter = DefaultLetter;
        if (!TextUtils.isEmpty(user.getNickname())) {
            letter = Pinyin.toPinyin(user.getNickname().toCharArray()[0]);
            user.setInitialLetter(letter.toUpperCase().substring(0, 1));
            if (isNumeric(user.getInitialLetter()) || !check(user.getInitialLetter())) {
                user.setInitialLetter("#");
            }
            return;
        }
        if (letter == DefaultLetter && !TextUtils.isEmpty(user.getNickname())) {
            letter = Pinyin.toPinyin(user.getNickname().toCharArray()[0]);
        }
        user.setInitialLetter(letter.substring(0, 1));
        if (isNumeric(user.getInitialLetter()) || !check(user.getInitialLetter())) {
            user.setInitialLetter("#");
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public static boolean check(String fstrData) {
        char c = fstrData.charAt(0);
        if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
            return true;
        } else {
            return false;
        }
    }

    //解决小米手机上获取图片路径为null的情况
    public static Uri getPictureUri(android.content.Intent intent, Activity activity) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = activity.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID},
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing
                } else {
                    Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }


    public static boolean sdCardIsAvailable() {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED))
            return false;
        return true;
    }

    public static boolean enoughSpaceOnSdCard(long updateSize) {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED))
            return false;
        return (updateSize < getRealSizeOnSdcard());
    }

    /**
     * get the space is left over on sdcard
     */
    public static long getRealSizeOnSdcard() {
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * Checks if there is enough Space on phone self
     */
    public static boolean enoughSpaceOnPhone(long updateSize) {
        return getRealSizeOnPhone() > updateSize;
    }

    /**
     * get the space is left over on phone self
     */
    public static long getRealSizeOnPhone() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        long realSize = blockSize * availableBlocks;
        return realSize;
    }

    /**
     * ???????????dp???px
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * ???????????? px(????) ???λ ???? dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f) - 15;
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @param  （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param  （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static void hideSoftInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        IBinder b = null;
        if (view != null) b = view.getWindowToken();
        imm.hideSoftInputFromWindow(b, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showSoftInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        IBinder b = null;
        if (view != null) b = view.getWindowToken();
        imm.showSoftInputFromInputMethod(b, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static boolean isBlank(String s) {
        return (s == null || s.equals("") || s.equals("null"));
    }

    public static boolean isBlank(Object s) {
        return (s == null || s.equals("") || s.equals("null"));
    }

    public static boolean isPhoneNumber(String phone) {
//		Pattern pattern = Pattern.compile("((13[0-9]|15[0-3|5-9]|18[0|1|2|3|5-9])\\d{8})");
        if (!isBlank(phone) && phone.length() == 11) {
            Pattern pattern = Pattern.compile("((13[0-9]|15[0-3|5-9]|18[0-9]|1349|17[0|6-8]|14[5|7])\\d{8})");
            Matcher matcher = pattern.matcher(phone);

            if (matcher.matches()) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean isMail(String mail) {
//		Pattern regex = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
        Pattern regex = Pattern.compile("\\w+((-\\w+)|(.\\w+))*@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+");
        Matcher matcher = regex.matcher(mail);
        return matcher.matches();
    }

    /**
     * 将 2013-07-22T09:30:14 格式化为 2013-07-22 09:30:14
     *
     * @param time
     * @return
     */
    public static String getFormatTime(String time) {
        if (isBlank(time)) {
            return time;
        }
        String[] s = time.split("T");
        if (s.length == 2) {
            time = s[0] + " " + s[1];
        }
        return time;
    }

    /**
     * 计算传入时间与当前时间的间隔
     * 例：3分钟前
     *
     * @param time
     * @return
     */
    public static String getShowValue(String time) {
        if (isBlank(time)) {
            return time;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date d1 = df.parse(time);
            Date d2 = new Date();
            long diff = d2.getTime() - d1.getTime();   // 这样得到的差值是微秒级别

            if (diff < 0) {
                return "0分钟前";
            }

            long days = diff / (1000 * 60 * 60 * 24);
            long minutes = diff / (1000 * 60);

            DateFormat dfhm = new SimpleDateFormat("HH:mm");
            DateFormat dfmd = new SimpleDateFormat("MM-dd");

			if(minutes<60){
				time=minutes+"分钟前";
			}else{
				time="今天 "+dfhm.format(d1);
			}
			if(days>=1){
				time=dfmd.format(d1)+" "+dfhm.format(d1);
			}

//            if (minutes < 60) {
//                time = minutes + "分钟前";
//            } else {
//                time = dfmd.format(d1);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    // 从网络获取时间
    // 格式 yyyy-MM-dd HH:mm:ss
    public static String getTimeFromInternet() {
        try {
            URL url = new URL("http://www.baidu.com");
            URLConnection uc = url.openConnection();
            uc.connect();
            long ld = uc.getDate();
            Date date = new Date(ld);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            return df.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isHigherVersion(String newv, String oldv) {

        String[] new_v = newv.split("\\.");
        String[] old_v = oldv.split("\\.");
        for (int i = 0; i < new_v.length; i++) {
            if (Integer.valueOf(old_v[i]) < Integer.valueOf(new_v[i])) {
                return true;
            } else if (Integer.valueOf(old_v[i]) == Integer.valueOf(new_v[i])) {
                continue;
            } else {
                break;
            }
        }

        return false;
    }


}
