package com.websocketim.manager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.FileUtils;
import com.websocketim.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlaySoundManager implements SensorEventListener {

    private final static String TAG = "PlaySoundManager";

    private Handler handler = null;

    private Context cxt = null;

    private MediaPlayer mMediaPlayer = null;

    private String baseSoundPath = "";

    private final static int WHAT_PLAY = 1;

    private AudioManager audioManager;
    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float range = event.values[0];

        if (range == sensor.getMaximumRange()) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);//打开扬声器
        } else if (range >= 5.0) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);//打开扬声器
        } else if (range < 5.0){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
        }else {
//            Log.d(TAG, "onSensorChanged: 听筒模式 ");
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }

        float[] values = event.values;
        StringBuilder sb = new StringBuilder();
        sb.append("\nX方向上的加速度：");
        sb.append(values[0]);
        sb.append("\nY方向上的加速度：");
        sb.append(values[1]);
        sb.append("\nZ方向上的加速度：");
        sb.append(values[2]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public interface PlaySoundFinishedListener {

        void onSoundFinished();
    }

    private PlaySoundFinishedListener playSoundFinishedListener = null;

//	private HashSet<String> downloadTask=new HashSet<String>();

    private List<String> downloadTask = Collections.synchronizedList(new ArrayList<String>());

    public PlaySoundManager(Context context, MediaPlayer mediaPlayer, String Master) {
        Log.d(TAG, "PlaySoundManager: ");
        this.cxt = context;
        this.mMediaPlayer = mediaPlayer;
        String appName = this.cxt.getPackageName();
        String master = Master;
        // 例子
        // /sdcard/msgcopy/<appName>/chat/<master>/<fileName>
        this.baseSoundPath = FileUtils.getChatPath() +
                master + File.separator;

        this.handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case WHAT_PLAY:
                        File f = new File((String) msg.obj);
                        if (f.exists() && null != mMediaPlayer && !mMediaPlayer.isPlaying()) {
                            try {
                                mMediaPlayer.reset();
                                mMediaPlayer.setDataSource(f.getPath());
                                mMediaPlayer.prepare();
                                mMediaPlayer.start();
                                mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        if (null != playSoundFinishedListener) {
                                            playSoundFinishedListener.onSoundFinished();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    default:
                        break;
                }
            }
        };
    }

    public boolean isPlaying(){
        if (null != mMediaPlayer && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            return true;
        }
        return false;
    }

    public void play(String path, PlaySoundFinishedListener l) {
        audioManager = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
        sensorManager = (SensorManager) cxt.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (!CommonUtil.isBlank(path)) {
            this.playSoundFinishedListener = l;
            boolean isPlaying = false;
            try {
                isPlaying = mMediaPlayer.isPlaying();
            }
            catch (IllegalStateException e) {
                mMediaPlayer = null;
                mMediaPlayer = new MediaPlayer();
            }
            if (null != mMediaPlayer && isPlaying) {
                mMediaPlayer.stop();
            }
            if (path.startsWith("http")) {
                // 如果当前的url正在下载中则不进行操作
                if (downloadTask.contains(path)) {
                    LogUtil.i(TAG, "file in download task...");
                    return;
                }
                // 查看该url是否已经下载
                String soundName = path.substring(path.lastIndexOf("/") + 1);
                if (new File(baseSoundPath + soundName).exists()) {
                    LogUtil.i(TAG, "file had downloaded, play it now");
                    notifyPlay(baseSoundPath + soundName);
                } else {
                    LogUtil.i(TAG, "file not found, start download...");
                    new Thread(new DownloadSound(path)).start();
                }
            } else {
                notifyPlay(path);
            }
        }
    }

    private void notifyPlay(String path) {
        Message msg = new Message();
        msg.what = WHAT_PLAY;
        msg.obj = path;
        handler.sendMessage(msg);
    }

    private class DownloadSound implements Runnable {

        private String url = "";

        public DownloadSound(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            LogUtil.i(TAG, "downloading " + url);
            downloadTask.add(url);
            InputStream is = null;
            OutputStream os = null;
            try {
                // 创建文件
                String soundName = url.substring(url.lastIndexOf("/") + 1);
                File soundFile = new File(baseSoundPath + soundName);
                File directory = soundFile.getParentFile();
                if (!directory.exists() && !directory.mkdirs()) {
                }
                try {

                    if (!soundFile.exists()) {
                        soundFile.createNewFile();
                    } else {
                        soundFile.delete();
                        soundFile.createNewFile();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                // 下载文件
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15 * 1000);
                conn.setReadTimeout(30 * 1000);

                is = conn.getInputStream();
                os = new FileOutputStream(soundFile);

                byte[] bs = new byte[1024];
                int len;
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }

                LogUtil.i(TAG, "download finished\nurl: " + url + "\nlocal: " + soundFile.getPath());
                notifyPlay(soundFile.getPath());

            } catch (Exception e) {
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
}
