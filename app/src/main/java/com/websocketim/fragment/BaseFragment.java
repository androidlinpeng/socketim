package com.websocketim.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.websocketim.R;

/**
 * Created by Administrator on 2017/11/2.
 */

public abstract class BaseFragment extends Fragment {

    protected boolean isUsable() {
        return getActivity() != null;
    }

    public void openActivity(Class<?> pClass) {
        openActivity(pClass, null);
    }

    public void openActivity(Class<?> pClass, Bundle pBundle) {
        Intent intent = new Intent(getActivity(), pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
    }

    @Override
    public void startActivity(Intent intent) {
        startActivity(intent, true);
    }

    public void startActivity(Intent intent, boolean anim) {
        super.startActivity(intent);
        if (anim) {
            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.nothing);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, true);
    }

    public void startActivityForResult(Intent intent, int requestCode, boolean anim) {
        super.startActivityForResult(intent, requestCode);
        if (anim) {
            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.nothing);
        }
    }
}
