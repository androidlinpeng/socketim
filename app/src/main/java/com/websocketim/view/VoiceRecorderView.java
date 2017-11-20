package com.websocketim.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.websocketim.R;

/**
 * Created by Administrator on 2017/9/22.
 */

public class VoiceRecorderView extends RelativeLayout {

    private static final String TAG = "VoiceRecorderView";

    private Context context;
    private ImageView micImage;
    private TextView recordingHint;
    protected Drawable[] micImages;


    public VoiceRecorderView(Context context) {
        super(context);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void startRecorder(int what) {
        Log.d(TAG, "startRecorder: ");
        micImage.setImageDrawable(micImages[what]);
    }

    public void showReleaseToCancelHint() {
        recordingHint.setText("松开手指，取消发送");
        recordingHint.setBackgroundResource(R.drawable.bg_recording_text_hint);
    }

    public void showMoveUpToCancelHint() {
        recordingHint.setText("手指上滑，取消发送");
        recordingHint.setBackgroundColor(Color.TRANSPARENT);
//        recordingHint.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("bg_recording_hint"));

    }

    private void init(Context context) {
        Log.d(TAG, "init: ");
        this.context = context;
        int LayoutId = R.layout.item_widget_voice_recorder;
        int mic_imageId = R.id.mic_image;
        int recording_hintId = R.id.recording_hint;
        View view = LayoutInflater.from(context).inflate(LayoutId, this);
        micImage = view.findViewById(mic_imageId);
        recordingHint = view.findViewById(recording_hintId);
        showMoveUpToCancelHint();

        micImages = new Drawable[]{
                getResources().getDrawable(R.drawable.record_animate_01),
                getResources().getDrawable(R.drawable.record_animate_02),
                getResources().getDrawable(R.drawable.record_animate_03),
                getResources().getDrawable(R.drawable.record_animate_04),
                getResources().getDrawable(R.drawable.record_animate_05),
                getResources().getDrawable(R.drawable.record_animate_06),
                getResources().getDrawable(R.drawable.record_animate_07),
                getResources().getDrawable(R.drawable.record_animate_08),
                getResources().getDrawable(R.drawable.record_animate_09),
                getResources().getDrawable(R.drawable.record_animate_10),
                getResources().getDrawable(R.drawable.record_animate_11),
                getResources().getDrawable(R.drawable.record_animate_12),
                getResources().getDrawable(R.drawable.record_animate_13),
                getResources().getDrawable(R.drawable.record_animate_14),};
    }

}
