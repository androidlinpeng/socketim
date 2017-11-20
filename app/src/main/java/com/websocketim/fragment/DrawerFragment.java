package com.websocketim.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.websocketim.R;
import com.websocketim.model.UserInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Administrator on 2017/11/1.
 */

public class DrawerFragment extends Fragment {

    private static final String TAG = "DrawerFragment";

    private ListView listview;
    private PlayerAdapter mAapter;
    private List<UserInfo> mList = new ArrayList<UserInfo>();

    private TextView time;

    public static void newInstance(String id, String data) {
        Log.d(TAG, "newInstance: " + id + data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drawer, null);

        this.time = rootView.findViewById(R.id.time);
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        this.time.setText(""+df.format(new Date()));

        this.listview = rootView.findViewById(R.id.listView);
        this.mAapter = new PlayerAdapter();
        String name[] = new String[]{"老师赵秀萍", "带你去旅行", "付凤兰", "苏云霞", "国际旅游岛", "南方姑娘-丽丽", "苏敏萍"};
        String id[] = new String[]{"4827", "-4834", "4851", "4870", "-4872", "4877", "4954"};
        for (int i = 0; i < name.length; i++) {
            UserInfo user = new UserInfo();
            user.setNickname(name[i]);
            user.setUsername(id[i]);
            mList.add(user);
        }
        this.mAapter.setData(mList);
        this.listview.setAdapter(mAapter);

        return rootView;
    }

    public class PlayerAdapter extends BaseAdapter {

        private List<UserInfo> list = new ArrayList<UserInfo>();

        private void setData(List<UserInfo> mlist) {
            this.list = mlist;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (null == convertView) {
                convertView = getLayoutInflater(getArguments()).inflate(R.layout.row_player_list_item, null);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.name);
                holder.score = convertView.findViewById(R.id.score);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            UserInfo user = (UserInfo) getItem(position);
            holder.name.setText(user.getNickname());
            holder.score.setText(user.getUsername());
            return convertView;
        }

    }

    static class ViewHolder {
        TextView name;
        TextView score;
    }

}
