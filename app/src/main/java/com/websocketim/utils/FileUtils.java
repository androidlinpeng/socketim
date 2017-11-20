package com.websocketim.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.loopj.android.http.RequestParams;
import com.websocketim.MyApplication;
import com.websocketim.asynchttp.APIHttp;
import com.websocketim.asynchttp.APIUrls;
import com.websocketim.asynchttp.ResultData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * Created by daniel on 15-6-16.
 */
public class FileUtils {

    public static String getPath() {
        String appName = MyApplication.getInstance().getPackageName();
        return Environment.getExternalStorageDirectory() + File.separator +
                "msgcopy" + File.separator +
                appName + File.separator;
    }

    public static String getMasterPath() {
        return getPath() + "master" + File.separator;
    }

    public static String getChatPath() {
        return getMasterPath() + "chat" + File.separator;
    }

    public static String getTempPath() {
        return getMasterPath() + "temp" + File.separator;
    }

    public static File createTempFile(String fileName) {
        if (!CommonUtil.isBlank(fileName)) {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File(getTempPath() + fileName);
                    file.getParentFile().mkdirs();
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    return file;
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    //创建网络视频缩列图
    public static Bitmap createVideoThumbnail(String url, File compressFile, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        try {
            FileOutputStream fos = new FileOutputStream(compressFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //创建本地视频缩列图
    public static File createVideoThumbnailFile(String master,File file) {
        String fileName = file.getName().split("\\.")[0] + "_THUMBNAIL.jpg";
        File compressFile = createAttachmentFile(master,fileName);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
        try {
            FileOutputStream fos = new FileOutputStream(compressFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compressFile;
    }


    // 创建一个附件文件
    public static File createAttachmentFile(String master, String fileName) {
        Context context = MyApplication.getInstance();
        if (!CommonUtil.sdCardIsAvailable()) {
            ToastUtils.showShort(context, "SD卡当前不可用");
            return null;
        }
        if (CommonUtil.isBlank(fileName)) {
            fileName = "_" + System.currentTimeMillis() + "_TMP";
        }
        // 例子
        // /sdcard/msgcopy/<appName>/chat/<master>/<fileName>
        String appName = context.getPackageName();
        String path = getChatPath() +
                master + File.separator +
                fileName;

        File file = new File(path);
        File directory = file.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    // 上传文件
    public static ResultData uploadFile(File file) throws FileNotFoundException {
        ResultData data = null;
        RequestParams requestParams = new RequestParams();
        requestParams.put("file", file);
        data = APIHttp.uploadFile(APIUrls.URL_UPLOAD_FILES, requestParams);
        return data;
    }


    public static void clearFile() {
        File file = new File(getMasterPath());
        delete(file);
    }

    /**
     * 删除指定路径文件
     *
     * @param path
     */
    public static boolean delete(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File f = new File(path);
        if (f.exists()) {
            f = renameOnDelete(f);
            return f.delete();
        } else {
            return false;
        }
    }

    private static File renameOnDelete(File file) {
        String tmpPath = file.getParent() + "/" + System.currentTimeMillis() + "_tmp";
        File tmpFile = new File(tmpPath);
        if (file.renameTo(tmpFile)) {
            return tmpFile;
        } else {
            return file;
        }
    }

    private static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
}
