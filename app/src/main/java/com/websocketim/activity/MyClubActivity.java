package com.websocketim.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.websocketim.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyClubActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "MyClubActivity";
    private String iamgeUrl1 = "http://img4.imgtn.bdimg.com/it/u=1923491115,2136109075&fm=27&gp=0.jpg";
    private String iamgeUrl2 = "http://f10.baidu.com/it/u=4108176325,1021243630&fm=72";
    private String iamgeUrl3 = "http://img2.imgtn.bdimg.com/it/u=3200373527,2802703123&fm=27&gp=0.jpg";
    private String iamgeUrl4 = "https://ss0.baidu.com/6ONWsjip0QIZ8tyhnq/it/u=3952827993,1779282683&fm=58";
    private String iamgeUrl5 = "http://img5.imgtn.bdimg.com/it/u=2919387663,1352833952&fm=27&gp=0.jpg";


    private ImageView back,addclub;
    private ListView listView;

    private MyAdapter myAdapter;

    // 头像的option
    private DisplayImageOptions options = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_club);

        int ic_head_defaultId = R.drawable.ic_head_default;
        this.options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .showImageForEmptyUri(ic_head_defaultId)
                .showImageOnFail(ic_head_defaultId)
                .showImageOnLoading(ic_head_defaultId)
                .displayer(new SimpleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
//                .displayer(new Displayer(0))
                .resetViewBeforeLoading(true)
                .build();

        this.back = (ImageView) findViewById(R.id.back);
        this.addclub = (ImageView) findViewById(R.id.addclub);
        this.listView = (ListView) findViewById(R.id.listView);

        this.back.setOnClickListener(this);
        this.addclub.setOnClickListener(this);

        myAdapter = new MyAdapter();
        List<ClubItem> items = new ArrayList<ClubItem>();
        items.add(new ClubItem(iamgeUrl1,"VIP1俱乐部","100/89"));
        items.add(new ClubItem(iamgeUrl2,"VIP2俱乐部","200/189"));
        items.add(new ClubItem(iamgeUrl3,"VIP3俱乐部","400/389"));
        items.add(new ClubItem(iamgeUrl4,"VIP4俱乐部","800/689"));
        items.add(new ClubItem(iamgeUrl5,"VIP5俱乐部","1600/1089"));
        listView.setAdapter(myAdapter);
        myAdapter.setData(items);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openActivity(ClubControlActivity.class);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.addclub:
                openActivity(CreateClubActivity.class);
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
            ImageLoader.getInstance().displayImage(item.icom, viewHolder.icom, options);

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
