package com.websocketim.activity;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.websocketim.R;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.FileUtils;
import com.websocketim.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class VideoPlayActivity extends Activity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "VideoPlayActivity";

    private String master = "";
    private String videoUrl = "";

    private boolean mediaShowing = false;

    private View mediaController = null;

    private View videoContainer = null;

    private VideoView videoView = null;

    private ImageButton play = null;

    private ImageButton againPlay = null;

    private TextView curMediaTime = null;

    private TextView mediaTime = null;

    private ProgressBar mediaProgress = null;

    private StringBuilder mFormatBuilder;

    private Formatter mFormatter;

    private View progress = null;

    private ImageView back;

    private static final int SHOW_PROGRESS = 1;
    private static final int SHOW_MEDIACONTROLLER = 2;
    private static final int WHAT_PLAY = 3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_PLAY:
                    File file = new File((String) msg.obj);
                    videoView.setVideoPath(file.getPath());
                    break;
                case SHOW_PROGRESS:
                    if (videoView.isPlaying()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setProgress();
                                if (videoView.isPlaying()) {
                                    sendEmptyMessage(SHOW_PROGRESS);
                                }
                            }
                        });
                    }
                    break;
                case SHOW_MEDIACONTROLLER:
                    mediaShowing = false;
                    mediaController.setVisibility(View.INVISIBLE);
                    break;
            }

        }
    };

    private String baseVideoPath = "";

    private List<String> downloadTask = Collections.synchronizedList(new ArrayList<String>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        videoUrl = getIntent().getStringExtra("attachVideo");
        master = getIntent().getStringExtra("master");
        if (CommonUtil.isBlank(videoUrl) || CommonUtil.isBlank(master)) {
            finish();
            return;
        } else {
            if (!videoUrl.startsWith("http")) {
                videoUrl = "file://" + videoUrl;
            }
        }

        String appName = this.getPackageName();
        // /sdcard/msgcopy/<appName>/chat/<master>/<fileName>
        this.baseVideoPath = FileUtils.getChatPath() +
                master + File.separator;

        this.mediaController = findViewById(R.id.media_controller);
        this.videoContainer = findViewById(R.id.video_container);
        this.videoView = findViewById(R.id.video_view);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.videoContainer.getLayoutParams();
        layoutParams.height = -1;
        this.videoContainer.setLayoutParams(layoutParams);
        this.videoView.setOnPreparedListener(this);
        this.videoView.setOnCompletionListener(this);
        play(videoUrl);
//        this.videoView.setVideoPath(this.videoUrl);

        this.back = findViewById(R.id.back);
        this.againPlay = findViewById(R.id.againPlay);
        this.play = findViewById(R.id.play);
        this.curMediaTime = findViewById(R.id.time_current);
        this.mediaTime = findViewById(R.id.time);
        this.progress = findViewById(R.id.progress);
        this.mediaProgress = findViewById(R.id.mediacontroller_progress);
        if (mediaProgress instanceof SeekBar) {
            SeekBar seekBar = (SeekBar) mediaProgress;
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        return;
                    }
                    long duration = videoView.getDuration();
                    long newposition = (duration * progress) / 1000L;
                    videoView.seekTo((int) newposition);
                    if (curMediaTime != null)
                        curMediaTime.setText(stringForTime((int) newposition));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    handler.removeMessages(SHOW_PROGRESS);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    setProgress();
                    updatePausePlay();
                    handler.sendEmptyMessage(SHOW_PROGRESS);
                }
            });
            mediaProgress.setMax(1000);
        }


        this.play.setOnClickListener(this);
        this.back.setOnClickListener(this);
        this.againPlay.setOnClickListener(this);
        this.videoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowMediaController();
            }
        });

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    private void play(String videoUrl) {
        if (videoUrl.startsWith("http")) {
            if (downloadTask.contains(videoUrl)) {
                LogUtil.i(TAG, "file in download task...");
                return;
            }
            String videoPath = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
            if (new File(baseVideoPath + videoPath).exists()) {
                LogUtil.i(TAG, "file had downloaded, play it now");
                this.videoView.setVideoPath(baseVideoPath + videoPath);
            } else {
                LogUtil.i(TAG, "file not found, start download...");
                new Thread(new DownloadSound(videoUrl)).start();
            }
        }
    }

    private class DownloadSound implements Runnable {
        String url = "";

        public DownloadSound(String videoUrl) {
            url = videoUrl;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: download " + url);
            downloadTask.add(url);
            InputStream is = null;
            OutputStream os = null;
            String videoName = url.substring(url.lastIndexOf("/") + 1);
            File videoPath = new File(baseVideoPath + videoName);
            File directory = videoPath.getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
            }
            try {
                if (!videoPath.exists()) {
                    videoPath.createNewFile();
                } else {
                    videoPath.delete();
                    videoPath.createNewFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            // 下载文件
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15 * 1000);
                conn.setReadTimeout(60 * 1000);

                is = conn.getInputStream();
                os = new FileOutputStream(videoPath);

                byte[] bs = new byte[1024];
                int len;
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
                Message msg = new Message();
                msg.what = WHAT_PLAY;
                msg.obj = videoPath.getPath();
                handler.sendMessage(msg);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != os) {
                        os.close();
                    }
                    if (null != is) {
                        is.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            downloadTask.remove(url);
        }
    }

    private void ShowMediaController() {
        if (!mediaShowing) {
            mediaShowing = true;
            mediaController.setVisibility(View.VISIBLE);
            handler.sendEmptyMessageDelayed(SHOW_MEDIACONTROLLER, 3000);
        }
    }

    public int setProgress() {
        int position = videoView.getCurrentPosition();
        int duration = videoView.getDuration();

        if (mediaProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mediaProgress.setProgress((int) pos);
            }
            int percent = videoView.getBufferPercentage();
            mediaProgress.setSecondaryProgress(percent * 10);
        }

        if (mediaTime != null)
            mediaTime.setText(stringForTime(duration));
        if (curMediaTime != null)
            curMediaTime.setText(stringForTime(position));

        return position;
    }


    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        progress.setVisibility(View.GONE);
        videoView.setBackgroundColor(Color.TRANSPARENT);
        videoView.start();
        handler.sendEmptyMessage(SHOW_PROGRESS);
        updatePausePlay();
    }

    private void updatePausePlay() {
        if (this.videoView.isPlaying()) {
            this.play.setImageResource(R.drawable.ic_media_pause);
        } else {
            this.play.setImageResource(R.drawable.ic_media_play);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        int playId = R.id.play;
        int backId = R.id.back;
        int againPlayId = R.id.againPlay;
        if (playId == id) {
            pauseVideo();
        } else if (backId == id) {
            finish();
        } else if (againPlayId == id) {
            pauseVideo();
        }
    }

    private void pauseVideo() {
        if (videoView.isPlaying()) {
            videoView.pause();
            handler.sendEmptyMessage(SHOW_PROGRESS);
        } else {
            this.againPlay.setVisibility(View.GONE);
            videoView.start();
            handler.sendEmptyMessage(SHOW_PROGRESS);
        }
        updatePausePlay();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        updatePausePlay();
        this.handler.removeCallbacksAndMessages(this);
        this.mediaProgress.setProgress(0);
        this.mediaProgress.setSecondaryProgress(0);
        this.curMediaTime.setText(stringForTime(0));
        this.againPlay.setVisibility(View.VISIBLE);
    }

}
