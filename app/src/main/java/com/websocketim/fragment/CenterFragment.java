package com.websocketim.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.websocketim.Constants;
import com.websocketim.R;
import com.websocketim.activity.CreateClubActivity;
import com.websocketim.manager.IMChatManager;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.MsgCache;
import com.websocketim.utils.ToastUtils;

/**
 * Created by Administrator on 2017/11/1.
 */

public class CenterFragment extends BaseFragment implements View.OnClickListener {

    private String master = "";
    private String avator = "";

    private EditText authcode;
    private Button bt_join;
    private Button bt_create;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_center, null);

        UserInfo userinfo = (UserInfo) MsgCache.get(getContext()).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
            avator = userinfo.getHead50();
        }

        this.authcode = rootView.findViewById(R.id.authcode);
        this.bt_join = rootView.findViewById(R.id.join);
        this.bt_create = rootView.findViewById(R.id.bt_create);

        this.bt_join.setOnClickListener(this);
        this.bt_create.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.join:
                String code = authcode.getText().toString().trim();
                if (!CommonUtil.isBlank(code)) {
                    if (code.equals("157")) {
                        IMChatManager.RedPacketstartChat(getActivity(), master,avator ,Constants.FRAGMENT_GROUP, code, "红包房间");
//                        authcode.setText("");
                    }else {
                        ToastUtils.showShort(getActivity(),"验证码不正确");
                    }
                } else {
                    ToastUtils.showShort(getActivity(), "验证码不能为空");
                }
                break;
            case R.id.bt_create:
                openActivity(CreateClubActivity.class);
                break;
        }
    }
}
