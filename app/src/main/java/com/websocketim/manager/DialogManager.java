package com.websocketim.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.websocketim.R;

/**
 * Created by Administrator on 2017/9/15.
 */

public class DialogManager {

    public static void showDialog(Activity act, String message, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener, DialogInterface.OnCancelListener cancelListener){
        showDialog(act, message, act.getString(R.string.websocketim_action_confirm), positiveListener, act.getString(R.string.websocketim_action_cancel), negativeListener, cancelListener);
    }

    public static void showDialog(Activity act, String message, String positiveText, DialogInterface.OnClickListener positiveListener, String negativeText, DialogInterface.OnClickListener negativeListener, DialogInterface.OnCancelListener cancelListener){
        if (null != act){
            AlertDialog alertDialog = new AlertDialog.Builder(act)
//                    .setTitle(UZResourcesIDFinder.getResStringID("websocketim_str_dialog_tip"))
                    .setMessage(message)
                    .setPositiveButton(positiveText, positiveListener)
                    .setNegativeButton(negativeText, negativeListener)
                    .setOnCancelListener(cancelListener)
                    .create();
            alertDialog.show();
        }
    }
}
