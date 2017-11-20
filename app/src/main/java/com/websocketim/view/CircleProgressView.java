package com.websocketim.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.websocketim.R;

/**
 * Created by Administrator on 2017/7/28.
 */

public class CircleProgressView extends View {

    private static final String TAG = "CircleProgressView";

    private int mMaxProgress = 100;

    private int mProgress = 100;

    private int mCircleLineStrokeWidth = 22;

    private int mTxtStrokeWidth = 2;

    // 画圆所在的距形区域
    private RectF mRectF;

    private Paint mPaint;

    private Paint mPaintC;

    private Context mContext;

    private String mTxtHint1;

    private String mTxtHint2;
    private int bgColor;
    private int progressColor;


    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);


        mContext = context;
        mRectF = new RectF();
        mPaint = new Paint();
        mPaintC = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();


        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }


//        // 设置画笔相关属性
//        mPaint.setAntiAlias(true);
//        mPaint.setColor(bgColor);
//        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
//        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

//        // 设置画笔相关属性
//        mPaint.setAntiAlias(true);
//        mPaint.setColor(bgColor);
//        canvas.drawColor(Color.TRANSPARENT);
//        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
//        mPaint.setStyle(Paint.Style.STROKE);
//        // 位置
//        mRectF.left = mCircleLineStrokeWidth / 2; // 左上角x
//        mRectF.top = mCircleLineStrokeWidth / 2; // 左上角y
//        mRectF.right = width - mCircleLineStrokeWidth / 2; // 左下角x
//        mRectF.bottom = height - mCircleLineStrokeWidth / 2; // 右下角y
//
////        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
////        canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2,mPaintC);
//
//        // 绘制圆圈，进度条背景
//        canvas.drawArc(mRectF, -90, 360, false, mPaint);
//        mPaint.setColor(progressColor);
//        canvas.drawArc(mRectF, -90, ((float) mProgress / mMaxProgress) * 360, false, mPaint);


        int mCircleWidth = 12;
        int centre = getWidth() / 2; // 获取圆心的x坐标
        int radius = centre - mCircleWidth / 2;// 半径

        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.websocketim_BgColor));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(2);
//        canvas.drawCircle(centre, centre, centre, mPaint);

        mPaint.setStrokeWidth(mCircleWidth); // 设置圆环的宽度
        mPaint.setAntiAlias(true); // 消除锯齿
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
//        mPaint.setColor(Color.WHITE); // 设置圆环的颜色
//        canvas.drawCircle(centre, centre, radius, mPaintC); // 画出圆环
        RectF rectf = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);
        mPaint.setColor(progressColor); // 设置圆环的颜色
        canvas.drawArc(rectf, -90, ((float) mProgress / mMaxProgress) * 360, false, mPaint); // 根据进度画圆弧


    }


    public int getMaxProgress() {
        return mMaxProgress;
    }


    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }


    public void setProgress(int progress) {
        this.mProgress = progress;
        this.invalidate();
    }


    public int getmProgress() {
        return mProgress;
    }


    public void setProgressNotInUiThread(int progress) {
        this.mProgress = progress;
        this.postInvalidate();
    }


    public String getmTxtHint1() {
        return mTxtHint1;
    }


    public void setmTxtHint1(String mTxtHint1) {
        this.mTxtHint1 = mTxtHint1;
    }


    public String getmTxtHint2() {
        return mTxtHint2;
    }


    public void setmTxtHint2(String mTxtHint2) {
        this.mTxtHint2 = mTxtHint2;
    }


    public int getBgColor() {
        return bgColor;
    }


    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }


    public int getProgressColor() {
        return progressColor;
    }


    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }


}
