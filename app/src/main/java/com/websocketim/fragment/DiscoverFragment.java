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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/11/1.
 */

public class DiscoverFragment extends BaseFragment implements View.OnClickListener{

    private String iamgeUrl1 = "http://img4.imgtn.bdimg.com/it/u=1923491115,2136109075&fm=27&gp=0.jpg";
    private String iamgeUrl2 = "http://f10.baidu.com/it/u=4108176325,1021243630&fm=72";
    private String iamgeUrl3 = "http://img2.imgtn.bdimg.com/it/u=3200373527,2802703123&fm=27&gp=0.jpg";
    private String iamgeUrl4 = "https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3952827993,1779282683&fm=58";
    private String iamgeUrl5 = "http://img5.imgtn.bdimg.com/it/u=2919387663,1352833952&fm=27&gp=0.jpg";

    private ListView listView;

    private MyAdapter myAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover,null);

        this.listView = rootView.findViewById(R.id.listView);

        myAdapter = new MyAdapter();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd  HH:mm");
        String time= df.format(new Date());
        List<ClubItem> items = new ArrayList<ClubItem>();
        items.add(new ClubItem(iamgeUrl1,"俱乐部1号房间",time));
        items.add(new ClubItem(iamgeUrl2,"俱乐部2号房间",time));
        items.add(new ClubItem(iamgeUrl3,"俱乐部交流群",time));
        items.add(new ClubItem(iamgeUrl4,"豹子交流群",time));
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
            if (convertView == null) {
                convertView = getLayoutInflater(getArguments()).inflate(R.layout.row_discover_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.icom = convertView.findViewById(R.id.icom);
                viewHolder.clubname = convertView.findViewById(R.id.clubname);
                viewHolder.time = convertView.findViewById(R.id.time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final ClubItem item = datas.get(position);
            viewHolder.clubname.setText(""+item.clubname);
            viewHolder.time.setText(""+item.time);
            Glide.with(getActivity()).load(item.icom).into(viewHolder.icom);
            return convertView;
        }
    }

    public static class ViewHolder {
        private CircleImageView icom;
        private TextView clubname;
        private TextView time;
    }

    private class ClubItem {

        private String icom;
        private String clubname;
        private String time;

        public ClubItem(String icom,String clubname, String time) {
            this.icom = icom;
            this.clubname = clubname;
            this.time = time;
        }

    }

}

