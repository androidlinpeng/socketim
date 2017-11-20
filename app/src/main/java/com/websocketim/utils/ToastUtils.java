package com.websocketim.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ToastUtils {
	
	private final static String TAG="ToastUtils";

	public static void showShort(Context context, int resId){
		String msg;
		try {
			msg=context.getResources().getString(resId);
		} catch (Exception e) {
			msg=TAG+": "+e.getMessage();
		}
		showShort(context, msg);
	}
	
	public static void showShort(Context context, String msg){
		if(null==context || null==msg || msg.equals("") ||  msg.length()==0){
            Log.d(TAG, "showShort: ");
            return;
		}
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static void showLong(Context context, int resId){
		String msg;
		try {
			msg=context.getResources().getString(resId);
		} catch (Exception e) {
			msg=TAG+": "+e.getMessage();
		}
		showShort(context, msg);
	}
	
	public static void showLong(Context context, String msg){
		if(null==context || null==msg || msg.equals("") ||  msg.length()==0){
			return;
		}
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
