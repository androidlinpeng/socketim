package com.websocketim.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.websocketim.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateClubActivity extends BaseActivity implements View.OnClickListener{

    private String avatorUrl = "http://img3.imgtn.bdimg.com/it/u=1492752078,3094009867&fm=27&gp=0.jpg";

    private ListView listView;

    private MyAdapter myAdapter;

    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_club);

        this.back = (ImageView) findViewById(R.id.back);
        this.back.setOnClickListener(this);

        this.listView = (ListView)findViewById(R.id.listView);

        myAdapter = new MyAdapter();
        List<ClubItem> items = new ArrayList<ClubItem>();
        items.add(new ClubItem(avatorUrl,"抢红包游戏俱乐部","100/89"));
        items.add(new ClubItem(avatorUrl,"发红包游戏俱乐部","200/189"));
        listView.setAdapter(myAdapter);
        myAdapter.setData(items);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                onBackPressed();
                break;
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
                convertView = getLayoutInflater().inflate(R.layout.view_me_club_item, null);
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

            Glide.with(getApplication()).load(item.icom).into(viewHolder.icom);

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
