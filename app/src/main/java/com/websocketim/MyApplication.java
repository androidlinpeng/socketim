package com.websocketim;

import android.app.Application;

import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.websocketim.asynchttp.Http;
import com.websocketim.service.SocketService;

/**
 * Created by liang on 2017/4/12.
 */

public class MyApplication extends Application {

    private static MyApplication myApplication;

    private SocketService socketService;

    private PersistentCookieStore cookieStore = null;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;

        this.cookieStore = new PersistentCookieStore(this);
        Http.getSyncHttpClient().setCookieStore(cookieStore);
        Http.getuploadFileHttpClient().setCookieStore(cookieStore);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static MyApplication getInstance() {
        return myApplication;
    }

    public SocketService getSocketService(){
        return getInstance().socketService;
    }

    public SocketService setSocketService(SocketService socketService){
        return getInstance().socketService = socketService;
    }


}
