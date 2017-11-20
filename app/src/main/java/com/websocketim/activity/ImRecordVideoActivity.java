package com.websocketim.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.websocketim.R;
import com.websocketim.utils.CommonUtil;
import com.websocketim.view.CircleProgressView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ImRecordVideoActivity extends Activity implements View.OnClickListener, MediaRecorder.OnErrorListener {

    private static final String TAG = "VideoRecordDemo";
    private String videoPath;

    static final int ACTIVITY_REQUEST_CODE_A = 100;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    private CircleProgressView circleProgress;
    private int progress;
    private Animator animator;
    private int recordTime = 10;
    private int t = 0;
    //进度线程
    private Thread mProgressThread;
    //当前进度/时间
    private int mProgress;
    private boolean isRunning;
    //判断是否正在录制
    private boolean isRecording = false;
    private File mRecAudioFile;

    private ImageView back;
    private ImageView toggle;
    private boolean isOpenLight = false;
    private int cameraPosition = 0;

    private boolean bl_click = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_record_video);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        if (currentVolume == 0) {
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }

        videoPath = getIntent().getStringExtra("videoFile");
        Log.d(TAG, "onCreate: videoPath" + videoPath);
        if (CommonUtil.isBlank(videoPath)) {
            finish();
            return;
        }

        initView();

        initSurfaceView();

    }

    private void initView() {

        surfaceView = findViewById(R.id.surfaceView);
        back = findViewById(R.id.back);
        toggle = findViewById(R.id.toggle);

        mMediaRecorder = new MediaRecorder();
        circleProgress = findViewById(R.id.cpView);
        //圆形进度条设置
        circleProgress.setProgressColor(Color.TRANSPARENT);
        ViewTreeObserver observerCircle = circleProgress.getViewTreeObserver();
        observerCircle.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                progress = circleProgress.getmProgress();
                return true;
            }
        });

        back.setOnClickListener(this
        );
        toggle.setOnClickListener(this);

        circleProgress.setOnTouchListener(new View.OnTouchListener() {

            private final static int WHAT_START = 1;
            private final static int WHAT_STOP = 2;
            private final static int WHAT_TIME = 3;

            private Timer timer = null;

            private Handler handler = new Handler() {

                @Override
                public void handleMessage(android.os.Message msg) {
                    switch (msg.what) {
                        case WHAT_START:
                            Log.d(TAG, "handleMessage: WHAT_START 1");
                            if (!isActionUp) {
                                Log.d(TAG, "handleMessage: WHAT_START  2");
                                timer = new Timer();
                                timer.schedule(new TimerTask() {

//                                    int t = 0;

                                    @Override
                                    public void run() {
                                        if (t >= 10) {
                                            timer.cancel();
                                            handler.sendEmptyMessage(WHAT_STOP);
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                }
                                            });
                                            return;
                                        }
                                        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                                        String sTime = sdf.format(t * 1000);
                                        Message msg = new Message();
                                        msg.what = WHAT_TIME;
                                        msg.obj = sTime;
                                        handler.sendMessage(msg);
                                        t++;
                                    }
                                }, 0, 1000);
                                circleProgress.setProgressColor(getResources().getColor(R.color.websocketim_ProgressColor));
                                startRecord();
                            }
                            break;
                        case WHAT_STOP:
                            Log.d(TAG, "handleMessage: WHAT_STOP ");
                            stopRecordSave();
                            handler.removeMessages(WHAT_STOP);
                            break;
                        case WHAT_TIME:
//                            soundTip.setText((String) msg.obj);
                            break;

                        default:
                            break;
                    }
                }
            };

            // 是否抬起
            private boolean isActionUp = true;

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                boolean ret = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onTouch: ACTION_DOWN ");
                        isActionUp = false;
                        Toast.makeText(getApplication(), "开始录制", Toast.LENGTH_SHORT).show();
                        handler.sendEmptyMessageDelayed(WHAT_START, 300);
                        ret = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: ACTION_UP ");
                        isActionUp = true;
                        if (null != timer) {
                            timer.cancel();
                        }
                        if (animator.isRunning()) {
                            circleProgress.setProgressColor(Color.TRANSPARENT);
                            animator.end();
                        }
                        if (mProgress < 3) {
                            if (mProgress > 1) {
                                stopRecordUnSave();
                            } else {
                                try {
                                    Thread.sleep(1000);
                                    stopRecordUnSave();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Toast.makeText(getApplication(), "时间太短", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        handler.sendEmptyMessage(WHAT_STOP);
                        ret = false;
                        break;

                    default:
                        break;
                }
                return ret;

//                boolean ret = false;
//                Log.d(TAG, "onTouch: ");
//                int id = view.getId();
//                int action = event.getAction();
//                int cpViewId = UZResourcesIDFinder.getResIdID("cpView");
//                if (id == cpViewId) {
//                    switch (action) {
//                        case MotionEvent.ACTION_DOWN:
//                            Log.d(TAG, "ACTION_DOWN ");
//                            if (bl_click) {
//                                bl_click = false;
//                                startRecord();
//                                circleProgress.setProgressColor(getResources().getColor(UZResourcesIDFinder.getResColorID("websocketim_ProgressColor")));
//                                Toast.makeText(getApplication(), "开始录制", Toast.LENGTH_SHORT).show();
//                                mProgressThread = new Thread() {
//                                    @Override
//                                    public void run() {
//                                        super.run();
//                                        try {
//                                            mProgress = 0;
//                                            isRunning = true;
//                                            while (isRunning) {
//                                                mProgress++;
//                                                Thread.sleep(1000);
//                                            }
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                };
//                                mProgressThread.start();
//                                ret = true;
//                            }
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            Log.d(TAG, "ACTION_UP ");
//                            if (animator.isRunning()) {
//                                circleProgress.setProgressColor(Color.TRANSPARENT);
//                                animator.end();
//                            }
//                            if (mProgress < 3) {
//                                //时间太短不保存
//                                if (mProgress > 1) {
//                                    stopRecordUnSave();
//                                } else {
//                                    try {
//                                        Thread.sleep(1000);
//                                        stopRecordUnSave();
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                                Toast.makeText(getApplication(), "时间太短", Toast.LENGTH_SHORT).show();
//                                break;
//                            }
//                            //停止录制
//                            stopRecordSave();
//                            ret = false;
//                            break;
//                    }
//                }
//                return ret;
            }
        });

    }

    private void initSurfaceView() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setFixedSize(176, 144);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(new SurfaceCallback());
    }


    private void stopRecordSave() {
        Log.d(TAG, "stopRecordSave ");
        if (isRecording) {
            bl_click = true;
            isRunning = false;
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.stop();
            isRecording = false;
            Toast.makeText(this, "录制完成", Toast.LENGTH_SHORT).show();
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String sTime = sdf.format(t * 1000);
            Intent intent = new Intent(ImRecordVideoActivity.this, IMChatActivity.class);
            intent.putExtra("videoTime", sTime);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ACTIVITY_REQUEST_CODE_A) {
            String result = data.getStringExtra("result");
            if (null != result) {
                try {
                    Intent resultData = new Intent();
                    JSONObject json = new JSONObject();
                    json.put("smallvideo", result);
                    resultData.putExtra("result", json.toString());
                    setResult(RESULT_OK, resultData);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopRecordUnSave() {
        Log.d(TAG, "stopRecordUnSave");
        if (isRecording) {
            bl_click = true;
            isRunning = false;
            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
            }catch (Exception e){
                e.printStackTrace();
            }
            isRecording = false;
            if (mRecAudioFile.exists()) {
                mRecAudioFile.delete();
            }
        }
    }

    private void startRecord() {
        //没有外置存储, 直接停止录制
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        mRecAudioFile = new File(videoPath);
        try {

            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setOnErrorListener(this);
            //从相机采集视频
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 从麦克采集音频信息
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置视频格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //编码格式
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mMediaRecorder.setMaxDuration(recordTime * 1000);
//            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//            mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            mMediaRecorder.setVideoSize(640, 480);
            //每秒的帧数
            mMediaRecorder.setVideoFrameRate(24);
            // 设置帧频率，然后就清晰了
            mMediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
            mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
            mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            //解决录制视频, 播放器横向问题
            if (cameraPosition == 0) {
                mMediaRecorder.setOrientationHint(getPreviewDegree(ImRecordVideoActivity.this));
            } else {
                mMediaRecorder.setOrientationHint(getPreviewDegree(ImRecordVideoActivity.this) + 180);
            }
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecording = true;
            recordAnimater();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPreviewDegree(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    private void recordAnimater() {
        startAnimator();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                circleProgress.setProgressColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void startAnimator() {
        animator = ObjectAnimator.ofInt(circleProgress, "progress", progress);
        animator.setDuration(recordTime * 1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int backId = R.id.back;
        int toggleId = R.id.toggle;
        if (id == backId) {
            finish();
        } else if (id == toggleId) {
            if (!isRecording) {
                toggleFrontAndBack();
            }
        }
    }

    private void toggleFrontAndBack() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 0) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    cameraPosition = 1;
                    startPreView(surfaceHolder, cameraPosition);
                    break;
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraPosition = 0;
                    startPreView(surfaceHolder, cameraPosition);
                    break;
                }
            }
        }
    }

    @Override
    public void onError(MediaRecorder mr, int i, int i1) {
        try {
            if (mr != null)
                mr.reset();
        } catch (Exception e) {
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated");
            surfaceHolder = holder;
            startPreView(holder, cameraPosition);

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Log.d(TAG, "surfaceChenged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceDestroyed");
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            if (mMediaRecorder != null) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }
    }

    private void startPreView(SurfaceHolder holder, int cameraPosition) {
        Log.d(TAG, "startPreView");
        if (mCamera == null) {
            mCamera = Camera.open(cameraPosition);
        } else {
            freeCameraResource();
            mCamera = Camera.open(cameraPosition);
        }
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        } else {
            mMediaRecorder.reset();
        }
        mCamera.setDisplayOrientation(90);

        try {
            mCamera.setPreviewDisplay(holder);
            Camera.Parameters parameters = mCamera.getParameters();
            List<String> focusMode = parameters.getSupportedFocusModes();
            if (focusMode != null) {
                for (String mode : focusMode) {
                    mode.contains("continuous-video");
                    parameters.setFocusMode("continuous-video");
                }
            }
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        freeCameraResource();
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}