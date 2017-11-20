package com.websocketim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.websocketim.service.SocketService;
import com.websocketim.utils.CommonUtil;

/**
 * Created by Administrator on 2017/10/31.
 */

public class MainReceiver extends BroadcastReceiver {

    private static final String TAG = "MainReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null != intent) {
            String action = intent.getAction();
//            //重启设备时的广播
//            Intent.ACTION_REBOOT;
//            //屏幕被关闭之后的广播
//            Intent.ACTION_SCREEN_OFF;
//            //屏幕被打开之后的广播
//            Intent.ACTION_SCREEN_ON;
//            //充电状态，或者电池的电量发生变化//电池的充电状态、电荷级别改变，不能通过组建声明接收这个广播，只有通过Context.registerReceiver()注册
//            Intent.ACTION_BATTERY_CHANGED;
            if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
                    || action.equals(Intent.ACTION_TIME_TICK)
                    || action.equals(Intent.ACTION_BATTERY_CHANGED)
                    || action.equals(Intent.ACTION_REBOOT)
                    || action.equals(Intent.ACTION_SCREEN_OFF)
                    || action.equals(Intent.ACTION_SCREEN_ON)) {
                if (!CommonUtil.isWorked(context, "com.websocketim.service.SocketService")) {
                    context.startService(new Intent(context, SocketService.class));
                    Log.d(TAG, "onReceive: SocketService null");
                } else {
                    Log.d(TAG, "onReceive: SocketService !null");
                }
            }
            Log.d(TAG, "onReceive: action " + action);
//            ToastUtils.showShort(MyApplication.getInstance(),""+action);
        }
    }
}
