package com.websocketim.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RelativeLayoutHasResizeListener extends RelativeLayout {

	private OnResizeListener mListener = null;

	public interface OnResizeListener {
		void OnResize(int w, int h, int oldw, int oldh);
	}

	public void setOnResizeListener(OnResizeListener l) {
		mListener = l;
	}

	public RelativeLayoutHasResizeListener(Context context) {
		super(context);
	}

	public RelativeLayoutHasResizeListener(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RelativeLayoutHasResizeListener(Context context, AttributeSet attrs,
                                           int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mListener != null) {
			mListener.OnResize(w, h, oldw, oldh);
		}
	}

}
