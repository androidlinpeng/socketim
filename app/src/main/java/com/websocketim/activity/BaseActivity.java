package com.websocketim.activity;

import android.content.Intent;
import android.os.Bundle;

import com.websocketim.R;


/**
 * Created by Administrator on 2017/10/31.
 */

public abstract class BaseActivity extends BaseSwipeBackActivity{

    public void openActivity(Class<?> pClass) {
        openActivity(pClass, null);
    }

    public void openActivity(Class<?> pClass, Bundle pBundle) {
        Intent intent = new Intent(this, pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        startActivity(intent, true);
    }

    public void startActivity(Intent intent, boolean anim){
        super.startActivity(intent);
        if (anim){
            overridePendingTransition(R.anim.push_left_in, R.anim.nothing);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, true);
    }

    public void startActivityForResult(Intent intent, int requestCode, boolean anim) {
        super.startActivityForResult(intent, requestCode);
        if (anim){
            overridePendingTransition(R.anim.push_left_in, R.anim.nothing);
        }
    }

    //    public void onBackPressed(boolean anim){
//        finish();
//        if(anim){
//            overridePendingTransition(R.anim.nothing,R.anim.push_right_out);
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing,R.anim.push_right_out);
    }

}
