package com.websocketim.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.websocketim.R;

/**
 * Created by Administrator on 2017/7/28.
 */

public class RoundnessView extends View {

    private static final String TAG = "CircleProgressView";


    private Paint mPaint;

    private Context mContext;

    private int maxColor;

    private int minColor;


    public RoundnessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundLoadView);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.RoundLoadView_maxcolor:
                    maxColor = a.getColor(attr, Color.GREEN);
                    break;
                case R.styleable.RoundLoadView_mincolor:
                    minColor = a.getColor(attr, Color.GREEN);
                    break;
            }
        }
        a.recycle();

        mContext = context;
        mPaint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();


        if (width != height) {
            int min = Math.min(width, height);
        }

        int mCircleWidth = 12;
        int centre = getWidth() / 2; // 获取圆心的x坐标
        int radius = centre - mCircleWidth / 2;// 半径

        mPaint.setAntiAlias(true);
        mPaint.setColor(maxColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(2);
        canvas.drawCircle(centre, centre, centre, mPaint);

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(minColor);
        canvas.drawCircle(centre, centre, radius, mPaint);

    }
}
