package com.websocketim.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.websocketim.R;
import com.websocketim.utils.FileUtils;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CaptureVideoActivity extends Activity implements SurfaceHolder.Callback {

        private static final String TAG = "video";

        private static final String EXTRA_DATA_FILE_NAME = "EXTRA_DATA_FILE_NAME";

        private static final int VIDEO_TIMES = 10;

        private static final int VIDEO_WIDTH = 320;

        private static final int VIDEO_HEIGHT = 240;

//    private static final int VIDEO_WIDTH = 640;
//    private static final int VIDEO_HEIGHT = 480;

        // context

        public Handler handler = new Handler();

        // media

        private MediaRecorder mediaRecorder;// 录制视频的类

        private Camera camera;
        private LinearLayout ll_back;
        // view

        private SurfaceView surfaceview;

        private SurfaceHolder surfaceHolder;

        private ImageView recordBtn;

        private ImageView recordingState;

        private TextView recordingTimeTextView;

        private ImageView switchCamera; // 切换摄像头

        // state

        private int cameraId = 0;

        private String filename;

        private boolean previewing = false;

        private boolean multiCamera = false;

        private boolean recording = false;

        private long start, end; // 录制时间控制

        private long duration = 0;

        private boolean destroyed = false;

        private int mAngle = 0;

        private LinkedList<Point> backCameraSize = new LinkedList<>();

        private LinkedList<Point> frontCameraSize = new LinkedList<>();

    public static void start(Activity activity, String videoFilePath, int captureCode) {
        Intent intent = new Intent();
        intent.setClass(activity, CaptureVideoActivity.class);
        intent.putExtra(EXTRA_DATA_FILE_NAME, videoFilePath);
        activity.startActivityForResult(intent, captureCode);
    }

    // 录制时间计数
    private Runnable runnable = new Runnable() {

        public void run() {
            end = new Date().getTime();
            duration = (end - start);
            int invs = (int) (duration / 1000);

            recordingTimeTextView.setText(secToTime(invs));

            // 录制过程中红点闪烁效果
            if (invs % 2 == 0) {
                //  recordingState.setBackgroundResource(R.drawable.nim_record_start);
            } else {
                //  recordingState.setBackgroundResource(R.drawable.nim_record_video);
            }
            if (invs >= VIDEO_TIMES) {
                stopRecorder();
                sendVideo();
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT); // 使得窗口支持透明度
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture_video);
        //   setTitle(R.string.video_record);

        parseIntent();
        findViews();
        initActionBar();

        setViewsListener();
        updateRecordUI();

        getVideoPreviewSize();

        surfaceview = (SurfaceView) this.findViewById(R.id.videoView);
        SurfaceHolder holder = surfaceview.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);

        resizeSurfaceView();
    }

    private void parseIntent() {
        filename = getIntent().getExtras().getString("videoFile");
    }

    private void findViews() {
        recordingTimeTextView = (TextView) findViewById(R.id.record_times);
        recordingState = (ImageView) findViewById(R.id.recording_id);

        recordBtn = (ImageView) findViewById(R.id.record_btn);
        switchCamera = (ImageView) findViewById(R.id.switch_cameras);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
    }

    private void initActionBar() {
        checkMultiCamera();
    }

    private void setViewsListener() {
        recordBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (recording) {
                    stopRecorder();
                    sendVideo();
                } else {
                    startRecorder();
                }
            }
        });
        switchCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @TargetApi(9)
    private void switchCamera() {
        if (Build.VERSION.SDK_INT >= 9) {
            cameraId = (cameraId + 1) % Camera.getNumberOfCameras();
        }
        resizeSurfaceView();
        shutdownCamera();
        initCamera();
        startPreview();
    }

    public void onResume() {
        super.onResume();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onPause() {
        super.onPause();

        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (recording) {
            stopRecorder();
            sendVideo();
        } else {
            shutdownCamera();
        }

    }

    public void onDestroy() {
        super.onDestroy();

        shutdownCamera();

        destroyed = true;
    }

    @Override
    public void onBackPressed() {
        if (recording) {
            stopRecorder();
        }

        shutdownCamera();

        setResult(RESULT_CANCELED);
        finish();
    }

    @SuppressLint("NewApi")
    private void getVideoPreviewSize(boolean isFront) {
        CamcorderProfile profile;
        int cameraId = 0;

        if (isCompatible(9)) {
            if (isFront) {
                cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }

        if (isCompatible(11)) {
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                if (profile != null) {
                    Point point = new Point();
                    point.x = profile.videoFrameWidth;
                    point.y = profile.videoFrameHeight;
                    if (isFront) {
                        frontCameraSize.addLast(point);
                    } else {
                        backCameraSize.addLast(point);
                    }
                }
            } else {
                Log.e(TAG, (isFront ? "Back Camera" : "Front Camera") + " no QUALITY_480P");
            }

            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_CIF)) {
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_CIF);
                if (profile != null) {
                    Point point = new Point();
                    point.x = profile.videoFrameWidth;
                    point.y = profile.videoFrameHeight;
                    if (isFront) {
                        frontCameraSize.addLast(point);
                    } else {
                        backCameraSize.addLast(point);
                    }
                }
            } else {
                Log.e(TAG, (isFront ? "Back Camera" : "Front Camera") + " no QUALITY_CIF");
            }

            if (isCompatible(15)) {
                if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
                    profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
                    if (profile != null) {
                        Point point = new Point();
                        point.x = profile.videoFrameWidth;
                        point.y = profile.videoFrameHeight;
                        if (isFront) {
                            frontCameraSize.addLast(point);
                        } else {
                            backCameraSize.addLast(point);
                        }
                    }
                } else {
                    Log.e(TAG, (isFront ? "Back Camera" : "Front Camera") + " no QUALITY_QVGA");
                }
            }
        }

        if (isCompatible(9)) {
            profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            if (profile == null) {
                Point point = new Point();
                point.x = 320;
                point.y = 240;
                if (isFront) {
                    frontCameraSize.addLast(point);
                } else {
                    backCameraSize.addLast(point);
                }
                Log.e(TAG, (isFront ? "Back Camera" : "Front Camera") + " no QUALITY_LOW");
            } else {
                Point point = new Point();
                point.x = profile.videoFrameWidth;
                point.y = profile.videoFrameHeight;
                if (isFront) {
                    frontCameraSize.addLast(point);
                } else {
                    backCameraSize.addLast(point);
                }
            }
        } else {
            if (!isFront) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
                if (profile == null) {
                    Point point = new Point();
                    point.x = 320;
                    point.y = 240;
                    backCameraSize.addLast(point);
                } else {
                    Point point = new Point();
                    point.x = profile.videoFrameWidth;
                    point.y = profile.videoFrameHeight;
                    backCameraSize.addLast(point);
                }
            }
        }
    }


    @SuppressLint("NewApi")
    private void getVideoPreviewSize() {
        backCameraSize.clear();
        frontCameraSize.clear();
        getVideoPreviewSize(false);
        if (Build.VERSION.SDK_INT >= 9) {
            if (Camera.getNumberOfCameras() >= 2) {
                getVideoPreviewSize(true);
            }
        }
    }

    private Point currentUsePoint = null;

    private void resizeSurfaceView() {
        Point point;
        if (cameraId == 0) {
            point = backCameraSize.getFirst();
        } else {
            point = frontCameraSize.getFirst();
        }
        if (currentUsePoint != null && point.equals(currentUsePoint)) {
            return;
        } else {
            currentUsePoint = point;
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int surfaceHeight = screenWidth * point.x / point.y;
            if (surfaceview != null) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) surfaceview.getLayoutParams();
                lp.width = screenWidth;
                lp.height = surfaceHeight;
                lp.addRule(13);
                surfaceview.setLayoutParams(lp);
            }
        }
    }


    @SuppressLint("NewApi")
    private void setCamcorderProfile() {
        CamcorderProfile profile;
        if (isCompatible(11)) {
            if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_CIF)) {
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_CIF);
            } else {
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            }
        } else {
            if (isCompatible(9)) {
                profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            }
        }

        if (profile != null) {
            if (currentUsePoint != null) {
                profile.videoFrameWidth = currentUsePoint.x;
                profile.videoFrameHeight = currentUsePoint.y;
            }

            profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;

            if (Build.MODEL.equalsIgnoreCase("MB525") || Build.MODEL.equalsIgnoreCase("C8812") || Build.MODEL.equalsIgnoreCase("C8650")) {
                profile.videoCodec = MediaRecorder.VideoEncoder.H263;
            } else {
                profile.videoCodec = MediaRecorder.VideoEncoder.H264;
            }

            if (Build.VERSION.SDK_INT < 11) {
                profile.videoCodec = MediaRecorder.VideoEncoder.H263;
            }

            if (Build.VERSION.SDK_INT >= 14) {
                profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
            } else {
                if (Build.DISPLAY != null && Build.DISPLAY.indexOf("MIUI") >= 0) {
                    profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
                } else {
                    profile.audioCodec = MediaRecorder.AudioEncoder.AMR_NB;
                }
            }
            mediaRecorder.setProfile(profile);
        } else {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);
        }
    }

    @SuppressLint("NewApi")
    private void setVideoOrientation() {
        if (Build.VERSION.SDK_INT >= 9) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            mediaRecorder.setOrientationHint(info.orientation);
        }
    }

    public void updateRecordUI() {
        if (recording) {
            recordBtn.setBackgroundResource(R.drawable.ic_im_take_video_btn);
        } else {
            recordBtn.setBackgroundResource(R.drawable.ic_im_take_video_btn);
        }
    }

    private boolean startRecorderInternal() throws Exception {
        shutdownCamera();
        if (!initCamera())
            return false;

        switchCamera.setVisibility(View.GONE);
        mediaRecorder = new MediaRecorder();

        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        setCamcorderProfile();

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setMaxDuration(1000 * VIDEO_TIMES);
        mediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);
        mediaRecorder.setOutputFile(filename);
        setVideoOrientation();

        mediaRecorder.prepare();
        mediaRecorder.start();

        return true;
    }

    private void startRecorder() {
        try {
            startRecorderInternal();
        } catch (Exception e) {
            Log.e(TAG, "start MediaRecord failed: " + e);
            Toast.makeText(this, "录制失败..."+e.getMessage(), Toast.LENGTH_SHORT).show();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.release();
            camera = null;
            return;

        }
        recording = true;
        start = new Date().getTime();
        handler.postDelayed(runnable, 1000);

        recordingTimeTextView.setText("00:00");

        updateRecordUI();
    }

    private void stopRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                // Log.w(TAG, getString(R.string.stop_fail_maybe_stopped));
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }

        handler.removeCallbacks(runnable);
        // recordingState.setBackgroundResource(R.drawable.nim_record_start);
        recording = false;
        updateRecordUI();
    }

    private void sendVideo() {
        File convertedFile = new File(filename);
        String message = "";
        if (convertedFile.exists()) {
            int b = (int) convertedFile.length();
            int kb = b / 1024;
            float mb = kb / 1024f;
//            message += mb > 1 ? getString(R.string.capture_video_size_in_mb, mb) : getString(
//                    R.string.capture_video_size_in_kb, kb);
//            message += getString(R.string.is_send_video);
            if (mb <= 1 && kb < 10) {
                tooShortAlert();
                return;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("是否发送？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                arg0.dismiss();
                                // sendVideo(null);
                                Intent intent = new Intent();
                                intent.putExtra("dur", duration);
                                intent.putExtra("path", filename);
                                intent.putExtra("videoTime", secToTime((int) (duration/1000)));
                                Log.d(TAG, "onClick: duration "+duration+"  "+secToTime((int) (duration/100)));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelRecord();
                    }
                })
                .setCancelable(false).show();


        if (!isFinishing() && !destroyed) {
            dialog.show();
        }
    }

    /**
     * 视频录制太短
     */
    private void tooShortAlert() {
//        EasyAlertDialogHelper.showOneButtonDiolag(this, null, getString(R.string.video_record_short), getString(R.string.iknow), true, new OnClickListener() {
//            @Override
//            public void onClick(View v) {
        cancelRecord();

//            }
//        });
    }

    /**
     * 取消重录
     */
    private void cancelRecord() {
        FileUtils.delete(filename);
        recordingTimeTextView.setText("00:00");
        shutdownCamera();
        initCamera();
        startPreview();
        checkMultiCamera();
    }

    /**
     * *************************************************** Camera Start ***************************************************
     */
    @SuppressLint("NewApi")
    public void checkMultiCamera() {
        if (Build.VERSION.SDK_INT >= 9) {
            if (Camera.getNumberOfCameras() > 1) {
                multiCamera = true;
                switchCamera.setVisibility(View.VISIBLE);
            } else {
                switchCamera.setVisibility(View.GONE);
            }
        } else {
            switchCamera.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NewApi")
    private boolean initCamera() {
        try {
            if (multiCamera) {
                camera = Camera.open(cameraId);
            } else {
                camera = Camera.open();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "init camera failed: " + e);
            // Toast.makeText(this, R.string.connect_vedio_device_fail, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (camera != null) {
            setCameraParameters();
        }

        return camera != null;
    }

    @SuppressLint("NewApi")
    private void setCameraParameters() {
        Camera.Parameters params = camera.getParameters();

        if (Build.VERSION.SDK_INT >= 15) {
            if (params.isVideoStabilizationSupported()) {
                params.setVideoStabilization(true);
            }
        }

        List<String> focusMode = params.getSupportedFocusModes();
        if (focusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        if (params != null) {
            mAngle = setCameraDisplayOrientation(this, cameraId, camera);
            Log.i(TAG, "camera angle = " + mAngle);
        }

        params.setPreviewSize(currentUsePoint.x, currentUsePoint.y);

        try {
            camera.setParameters(params);
        } catch (RuntimeException e) {
            Log.e(TAG, "setParameters failed", e);

        }
    }

    private void shutdownCamera() {
        if (camera != null) {
            if (previewing) {
                camera.stopPreview();
            }
            camera.release();
            camera = null;
            previewing = false;
        }
    }


    /**
     * **************************** SurfaceHolder.Callback Start *******************************
     */

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;

        shutdownCamera();
        if (!initCamera())
            return;
        startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceHolder = null;
        mediaRecorder = null;
    }

    /**
     * ************************ SurfaceHolder.Callback Start ********************************
     */

    private void startPreview() {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            previewing = true;
        } catch (Exception e) {
            //  Toast.makeText(this, R.string.connect_vedio_device_fail, Toast.LENGTH_SHORT).show();
            shutdownCamera();
            e.printStackTrace();
        }
    }

    /**
     * ********************************* camera util ************************************
     */
    @SuppressLint("NewApi")
    public int setCameraDisplayOrientation(Context context, int cameraId, Camera camera) {
        int orientation = 90;
        boolean front = (cameraId == 1);
        if (Build.VERSION.SDK_INT >= 9) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            orientation = info.orientation;
            front = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = manager.getDefaultDisplay().getRotation();
        int activityOrientation = roundRotation(rotation);
        int result;
        if (front) {
            result = (orientation + activityOrientation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (orientation - activityOrientation + 360) % 360;
            //遇到过一个小米1s后置摄像头旋转180°，但是不确定是不是所有小米1s都是这样的. 先做一个适配,以后有问题再说.
            if ("Xiaomi_MI-ONE Plus".equalsIgnoreCase(Build.MANUFACTURER + "_" + Build.MODEL)) {
                result = 90;
            }
        }
        camera.setDisplayOrientation(result);
        return result;
    }

    private int roundRotation(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else retStr = "" + i;
        return retStr;
    }

    public boolean isCompatible(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

}