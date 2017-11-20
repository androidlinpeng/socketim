package com.websocketim.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.websocketim.R;
import com.websocketim.view.PhotoViewPager;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

public class GalleryActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "GalleryActivity";
    private PhotoViewPager viewPager = null;
    private GalleryPageAdapter adapter = null;
    private TextView pageCount = null;
    private ImageView back;
    private List<String> imgs = null;
    private int pageIndex = 0;
    // 图片的option
    private DisplayImageOptions optionsImg = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(false);
        //去掉状态了
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        //透明/隐藏状态栏
//        if (Build.VERSION.SDK_INT >= 21) {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(option);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
        setContentView(R.layout.activity_gallery);

        int ic_empty_photoId = R.drawable.ic_empty_photo;
        this.optionsImg = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnFail(ic_empty_photoId)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(600, true, true, false))
                .resetViewBeforeLoading(true)
                .build();

        initData();

        initView();

    }

    private void initData() {
        this.pageIndex = 0;
        this.imgs = new ArrayList<String>();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            ArrayList list = bundle.getParcelableArrayList("data");
            this.imgs = (List<String>) list.get(0);
            this.pageIndex = bundle.getInt("index", 0);
            Log.d(TAG, "initData: pageIndex " + pageIndex);
        }
    }

    private void initView() {

        this.back = (ImageView) findViewById(R.id.back);
        this.pageCount = (TextView) findViewById(R.id.page_count);
        this.back.setOnClickListener(this);

        if (imgs.size() == 1) {
            this.pageCount.setVisibility(View.GONE);
        }
        if (pageIndex >= imgs.size()) {
            pageIndex = 0;
        }
        this.pageCount.setText((pageIndex + 1) + "/" + imgs.size());

        this.adapter = new GalleryPageAdapter();
        this.adapter.setDatas(this.imgs);
        this.viewPager = (PhotoViewPager) findViewById(R.id.viewpager);
        this.viewPager.setOnPageChangeListener(this);
        this.viewPager.setAdapter(adapter);
        this.viewPager.setCurrentItem(pageIndex);
        this.viewPager.setOffscreenPageLimit(0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int backId = R.id.back;
        if (id == backId) {
            onBackPressed();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.pageCount.setText((position + 1) + "/" + imgs.size());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class GalleryPageAdapter extends PagerAdapter {

        private List<String> datas = new ArrayList<String>();

        public GalleryPageAdapter() {
        }

        public void setDatas(List<String> datas) {
            this.datas.clear();
            this.datas.addAll(datas);
            notifyDataSetChanged();
        }

        public String getImgDescr(int position) {
            return this.datas.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int LayoutId = R.layout.view_page_gallery_item;
            View view = LayoutInflater.from(container.getContext()).inflate(LayoutId, container, false);
            PhotoView photoView = view.findViewById(R.id.img);
            final ProgressBar progressBar = view.findViewById(R.id.progress);
            String url = datas.get(position);
            if (url.startsWith("http")) {
                ImageLoader.getInstance().displayImage(url, photoView, optionsImg);
            } else {
                ImageLoader.getInstance().displayImage("file://" + url, photoView, optionsImg);
            }
            container.addView(view, -1, -1);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return this.datas.size();
        }
    }
}
