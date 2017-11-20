package com.websocketim.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.websocketim.Constants;
import com.websocketim.R;
import com.websocketim.activity.ContactListActivity;
import com.websocketim.activity.LoginActivity;
import com.websocketim.activity.MyClubActivity;
import com.websocketim.activity.PersonalEditActivity;
import com.websocketim.activity.RechargeActivity;
import com.websocketim.activity.SettingsActivity;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.GlideLoader;
import com.websocketim.utils.MsgCache;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/11/1.
 */

public class MyFragment extends BaseFragment {

    private static final String TAG = "MyFragment";
    private String avatorUrl = "";

    private static final int REQUEST_USER_LOGIN = 1;

    private ImageView avatar;
    private View center;
    private TextView username;
    private ListView listView;
    private MyAdapter myAdapter;
    private List<SettingItem> list = new ArrayList<SettingItem>();

    private String master;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mine, null);

        initView(rootView);

        initData();

        return rootView;
    }

    private void initView(View rootView) {
        UserInfo userinfo = (UserInfo) MsgCache.get(getActivity()).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
            avatorUrl = userinfo.getHead50();
        }
        listView = rootView.findViewById(R.id.listView);
        avatar = rootView.findViewById(R.id.avatar);
        center = rootView.findViewById(R.id.center);
        username = rootView.findViewById(R.id.username);
        if (!CommonUtil.isBlank(master)){
            GlideLoader.LoderCircleAvatar(getContext(),avatorUrl,this.avatar);
            username.setText(""+master);
        }else {
        }
        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CommonUtil.isBlank(master)){
                    openActivity(PersonalEditActivity.class);
                }else {
                    Intent intent = new Intent(getActivity(),LoginActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("personalCenter",true);
                    intent.putExtras(bundle);
                    startActivityForResult(intent,REQUEST_USER_LOGIN);
                }
            }
        });

    }

    private void initData() {
        myAdapter = new MyAdapter();
        List<SettingItem> items = new ArrayList<SettingItem>();
        items.add(new SettingItem(2,"钻石"));
        items.add(new SettingItem(1,"充值"));
        items.add(new SettingItem(1,"我的好友"));
        items.add(new SettingItem(1,"我的俱乐部"));
        items.add(new SettingItem(1,"设置"));
        listView.setAdapter(myAdapter);
        myAdapter.setData(items);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_USER_LOGIN:
                    UserInfo userinfo = (UserInfo) MsgCache.get(getActivity()).getAsObject(Constants.USER_INFO);
                    if (!CommonUtil.isBlank(userinfo)) {
                        master = userinfo.getUsername();
                        Glide.with(getContext()).load(avatorUrl).into(avatar);
                        username.setText(""+master);
                    }
                    break;
            }
        }
    }

    public class MyAdapter extends BaseAdapter {

        private List<SettingItem> datas = new ArrayList<SettingItem>();

        public void setData(List<SettingItem> datas){
            if (null != datas){
                this.datas.clear();
                notifyDataSetChanged();
                this.datas.addAll(datas);
            }
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            int resIcon = 0;
            if (convertView == null) {
                convertView = getLayoutInflater(getArguments()).inflate(R.layout.view_me_center_item, null);
                viewHolder = new ViewHolder();
                viewHolder.image = convertView.findViewById(R.id.image);
                viewHolder.abvance = convertView.findViewById(R.id.abvance);
                viewHolder.text = convertView.findViewById(R.id.text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final SettingItem item = datas.get(position);
            if (item.name.equals("钻石")){
                resIcon = R.drawable.socket_icom_diamond;
            }else if (item.name.equals("充值")){
                resIcon = R.drawable.socket_icom_recharge;
            }else if (item.name.equals("我的好友")){
                resIcon = R.drawable.socket_icom_my_friends;
            }else if (item.name.equals("我的俱乐部")){
                resIcon = R.drawable.socket_icom_me_club;
            }else if (item.name.equals("设置")){
                resIcon = R.drawable.socket_icom_settings;
            }
            viewHolder.image.setImageResource(resIcon);
            viewHolder.text.setText(item.name);
            viewHolder.abvance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.name.equals("钻石")){
                    }else if (item.name.equals("充值")){
                        openActivity(RechargeActivity.class);
                    }else if (item.name.equals("我的好友")){
                        openActivity(ContactListActivity.class);
                    }else if (item.name.equals("我的俱乐部")){
                        openActivity(MyClubActivity.class);
                    }else if (item.name.equals("设置")){
                        openActivity(SettingsActivity.class);
                    }
                }
            });

            return convertView;
        }
    }

    public static class ViewHolder {
        private ImageView image;
        private TextView text;
        private View abvance;
    }

    private class SettingItem {

        private int type;
        private String name;

        public SettingItem(int type, String name) {
            this.type = type;
            this.name = name;
        }

    }

}
