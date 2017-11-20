package com.websocketim.view;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.viewpagerindicator.CirclePageIndicator;
import com.websocketim.DBHelper;
import com.websocketim.R;
import com.websocketim.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniel on 15-7-7.
 */
public class EmotionContainerView extends RelativeLayout {

    private static final int EMOTION_NUM_PER_PAGE = 20;


    private ViewPager viewPager = null;

    private CirclePageIndicator circlePageIndicator = null;

    private EmotionPagerAdapter emotionPagerAdapter = null;

    private EditText editText = null;

    private View toggleView = null;

    private Activity activity = null;


    private HashMap<String, String> emotionMap = new HashMap<String, String>();

    public EmotionContainerView(Context context) {
        super(context);
        init();
    }

    public EmotionContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EmotionContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SQLiteDatabase sql = DBHelper.getEmotionDatabase();
        Cursor cur = sql.rawQuery("select * from emotion", null);
        while (cur.moveToNext()) {
            String key = cur.getString(cur.getColumnIndex("e_name")); // e1.png
            String value = cur.getString(cur.getColumnIndex("e_descr")); // [开心]
            emotionMap.put(key, value);
        }
        cur.close();
        sql.close();
        int count = emotionMap.size();
        int page = count / EMOTION_NUM_PER_PAGE;
        if (count % EMOTION_NUM_PER_PAGE != 0) {
            page++;
        }

        List<View> views = new ArrayList<View>();
        for (int i = 0; i < page; i++) {
            GridView gridView = new GridView(getContext());
            gridView.setNumColumns(7);
            gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            gridView.setVerticalSpacing(CommonUtil.dip2px(getContext(), 8));
            gridView.setGravity(Gravity.CENTER);
            ViewPager.LayoutParams gridParamas = new ViewPager.LayoutParams();
            gridParamas.height = CommonUtil.dip2px(getContext(), 106);
            gridView.setLayoutParams(gridParamas);
            gridView.setSelector(R.color.websocketim_transparent);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (null != editText){
                        if (position == parent.getCount() - 1){
                            // 删除按钮
                            int index = editText.getSelectionStart();
                            Editable editable = editText.getText();
                            String body = editable.toString();
                            if (!CommonUtil.isBlank(body)) {
                                if (String.valueOf(body.charAt(index - 1)).equals("]")) {
                                    int start = body.lastIndexOf("[", index);
                                    if (start != -1) {
                                        String key = body.substring(start, index);
                                        if (emotionMap.containsValue(key)){
                                            if (editable.length() != 0) {
                                                editable.delete(start, index);
                                            }
                                        }
                                    }
                                } else {
                                    if (editable.length() != 0 && index != 0) {
                                        editable.delete(index - 1, index);
                                    }
                                }
                            }
                        }else{
                            // 表情按钮
                            Drawable d = (Drawable) parent.getAdapter().getItem(position);
                            String descr = ((EmotionGridAdapter) parent.getAdapter()).getEmotionDescr(position);
                            ImageSpan imageSpan = new ImageSpan(d, descr);
                            SpannableString spannableString = new SpannableString(descr);
                            spannableString.setSpan(imageSpan, 0, descr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                            int index = editText.getSelectionStart();
                            Editable editable = editText.getText();
                            if (index < 0 || index >= editable.length()) {
                                editable.append(spannableString);
                            } else {
                                editable.insert(index, spannableString);
                            }
                        }
                    }
                }
            });

            EmotionGridAdapter adapter = new EmotionGridAdapter(EMOTION_NUM_PER_PAGE, (i + 1) * EMOTION_NUM_PER_PAGE);
            gridView.setAdapter(adapter);

            views.add(gridView);
        }


        this.viewPager = new ViewPager(getContext());
        this.viewPager.setOverScrollMode(ViewPager.OVER_SCROLL_NEVER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.setMargins(0, CommonUtil.dip2px(getContext(), 8), 0, 0);
        this.viewPager.setLayoutParams(layoutParams);
        this.emotionPagerAdapter = new EmotionPagerAdapter(views);
        this.viewPager.setAdapter(emotionPagerAdapter);

        this.circlePageIndicator = new CirclePageIndicator(getContext());
        RelativeLayout.LayoutParams indicatorParams = new RelativeLayout.LayoutParams(-2, -2);
        indicatorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        indicatorParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        indicatorParams.setMargins(0 ,0, 0, CommonUtil.dip2px(getContext(), 8));
        this.circlePageIndicator.setLayoutParams(indicatorParams);
        int paddingSize = CommonUtil.dip2px(getContext(), 4);
        this.circlePageIndicator.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
        this.circlePageIndicator.setViewPager(viewPager);
        this.circlePageIndicator.setFillColor(getResources().getColor(R.color.websocketim_Half));
        this.circlePageIndicator.setStrokeColor(getResources().getColor(R.color.websocketim_Dark));

        addView(viewPager);
        addView(circlePageIndicator);
    }

    public void setEditText(EditText editText){
        if (null != editText){
            this.editText = editText;
            this.editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    setVisibility(View.GONE);
                    return false;
                }
            });
        }
    }

    public void setToggleView(View view){
        if (null != view){
            this.toggleView = view;
            this.toggleView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getVisibility() == View.VISIBLE) {
                        setVisibility(View.GONE);
                    } else if (getVisibility() == View.GONE) {
                        if (getContext() instanceof Activity) {
                            CommonUtil.hideSoftInput((Activity)getContext());
                        }
                        setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public boolean canFinishActivity() {
        if (getVisibility() == View.VISIBLE) {
            setVisibility(View.GONE);
            return false;
        }
        return true;
    }


    public class EmotionPagerAdapter extends PagerAdapter {

        private List<View> views = null;

        public EmotionPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return this.views.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(this.views.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (this.views.get(position).getParent() == null) {
                ((ViewPager) container).addView(this.views.get(position), 0);
            } else {
                ((ViewGroup) this.views.get(position).getParent())
                        .removeView(this.views.get(position));
                ((ViewPager) container).addView(this.views.get(position), 0);
            }

            return views.get(position);
        }
    }


    public class EmotionGridAdapter extends BaseAdapter {

        // 表情总数
        private int totalEmotionCount;
        // 当前表情index
        private int emotionIndex;
        // 表情资源列表
        private List<Drawable> imgDrawable = new ArrayList<Drawable>();

        private List<String> emotionDescr = new ArrayList<String>();

        public EmotionGridAdapter(int total, int index) {
            this.totalEmotionCount = total;
            this.emotionIndex = index;
            init();
        }

        // 初始化表情资源
        private void init() {
            try {
                for (int i = emotionIndex - totalEmotionCount; i < emotionIndex; i++) {
                    if ( i + 1 > 50){
                        // 一共就50个表情
                        break;
                    }
                    String value = "e" + (i + 1) + ".png";
                    Drawable d = Drawable.createFromStream(getContext().getAssets().open("emotion/"+value), null);
                    this.imgDrawable.add(d);
                    this.emotionDescr.add(emotionMap.get(value));
                }
                // 在最后一个位置添加删除按钮
                Drawable d = Drawable.createFromStream(getContext().getAssets().open("emotion/emotion_delete.png"), null);
                imgDrawable.add(d);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public String getEmotionDescr(int position){
            return this.emotionDescr.get(position);
        }

        @Override
        public int getCount() {
            return imgDrawable.size();
        }

        @Override
        public Object getItem(int position) {
            return this.imgDrawable.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(getContext());
                int w = CommonUtil.dip2px(getContext(), 30);
                imageView.setLayoutParams(new GridView.LayoutParams(w, w));//设置ImageView对象布局
                imageView.setAdjustViewBounds(false);//设置边界对齐
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);//设置刻度的类型
                imageView.setPadding(8, 8, 8, 8);//设置间距
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageDrawable(imgDrawable.get(position));
            return imageView;
        }


    }
}
