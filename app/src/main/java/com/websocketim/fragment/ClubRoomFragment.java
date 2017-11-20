package com.websocketim.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.websocketim.R;
import com.websocketim.activity.ClubControlActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/11/9.
 */

public class ClubRoomFragment extends BaseFragment implements View.OnClickListener{

    private String iamgeUrl1 = "http://img4.imgtn.bdimg.com/it/u=1923491115,2136109075&fm=27&gp=0.jpg";
    private String iamgeUrl2 = "http://f10.baidu.com/it/u=4108176325,1021243630&fm=72";
    private String iamgeUrl3 = "http://img2.imgtn.bdimg.com/it/u=3200373527,2802703123&fm=27&gp=0.jpg";
    private String iamgeUrl4 = "https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3952827993,1779282683&fm=58";
    private String iamgeUrl5 = "http://img5.imgtn.bdimg.com/it/u=2919387663,1352833952&fm=27&gp=0.jpg";


    private ListView listView;

    private MyAdapter myAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_club_room, null);

        this.listView = (ListView) rootView.findViewById(R.id.listView);

        myAdapter = new MyAdapter();
        List<ClubItem> items = new ArrayList<ClubItem>();
        items.add(new ClubItem(iamgeUrl1,"VIP1游戏","100/89"));
        items.add(new ClubItem(iamgeUrl2,"VIP2游戏","200/189"));
        items.add(new ClubItem(iamgeUrl3,"VIP3游戏","400/389"));
        items.add(new ClubItem(iamgeUrl4,"VIP4游戏","800/689"));
        items.add(new ClubItem(iamgeUrl5,"VIP5游戏","1600/1089"));
        listView.setAdapter(myAdapter);
        myAdapter.setData(items);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openActivity(ClubControlActivity.class);
            }
        });

        return rootView;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

        }
    }

    public class MyAdapter extends BaseAdapter {

        private List<ClubItem> datas = new ArrayList<ClubItem>();

        public void setData(List<ClubItem> datas){
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
                convertView = getLayoutInflater(getArguments()).inflate(R.layout.view_me_club_item, null);
                viewHolder = new ViewHolder();
                viewHolder.icom = convertView.findViewById(R.id.icom);
                viewHolder.clubname = convertView.findViewById(R.id.clubname);
                viewHolder.member = convertView.findViewById(R.id.member);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final ClubItem item = datas.get(position);
            viewHolder.clubname.setText(""+item.clubname);
            viewHolder.member.setText(""+item.member);

            Glide.with(getActivity()).load(item.icom).into(viewHolder.icom);

            return convertView;
        }
    }

    public static class ViewHolder {
        private CircleImageView icom;
        private TextView clubname;
        private TextView member;
    }

    private class ClubItem {

        private String icom;
        private String clubname;
        private String member;

        public ClubItem(String icom,String clubname, String member) {
            this.icom = icom;
            this.clubname = clubname;
            this.member = member;
        }

    }

}
