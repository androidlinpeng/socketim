//package com.websocketim.moduleDemo;
//
//import android.content.Intent;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
//import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
//import com.uzmap.pkg.openapi.APICloud;
//import com.uzmap.pkg.uzcore.UZWebView;
//import com.uzmap.pkg.uzcore.uzmodule.UZModule;
//import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
//import com.uzmap.pkg.uzkit.request.APICloudHttpClient;
//import com.websocketim.Constants;
//import com.websocketim.activity.TestActivity;
//import com.websocketim.model.UserInfo;
//import com.websocketim.utils.CommonUtil;
//import com.websocketim.utils.MsgCache;
//
//public class APIModuleDemo extends UZModule {
//
//	private static final String TAG = "APIModuleDemo";
//	static final int ACTIVITY_REQUEST_CODE_A = 100;
//	private String master;
//
//	private UZModuleContext mJsCallback;
//
//	public APIModuleDemo(UZWebView webView) {
//		super(webView);
//		Log.d(TAG, "APIModuleDemo: ");
//
//		APICloud.initialize(mContext);
//		APICloudHttpClient.createInstance(mContext);
//
//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
//				.threadPriority(Thread.NORM_PRIORITY - 2)
//				.denyCacheImageMultipleSizesInMemory()
//				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
//				.tasksProcessingOrder(QueueProcessingType.LIFO)
//				.build();
//		ImageLoader.getInstance().init(config);
//
//	}
//
//	public void jsmethod_startService(UZModuleContext moduleContext){
//		mJsCallback = moduleContext;
//		UserInfo userInfo = new UserInfo();
//		userInfo.setMaster(moduleContext.optString("master"));
//		userInfo.setAvatar(moduleContext.optString("avatar"));
//		MsgCache.get(mContext).put(Constants.USER_INFO, userInfo);
//		master = moduleContext.optString("master");
//		startActivity();
//	}
//	public void jsmethod_stopService(UZModuleContext moduleContext){
//		mJsCallback = moduleContext;
//		UserInfo userInfo = new UserInfo();
//		userInfo.setMaster(moduleContext.optString("master"));
//		userInfo.setAvatar(moduleContext.optString("avatar"));
//		MsgCache.get(mContext).put(Constants.USER_INFO, userInfo);
//		master = moduleContext.optString("master");
//		startActivity();
//	}
//
//	public void jsmethod_Service(UZModuleContext moduleContext){
//		mJsCallback = moduleContext;
//		UserInfo userInfo = new UserInfo();
//		userInfo.setMaster(moduleContext.optString("master"));
//		userInfo.setAvatar(moduleContext.optString("avatar"));
//		MsgCache.get(mContext).put(Constants.USER_INFO, userInfo);
//		master = moduleContext.optString("master");
//		startActivity();
//	}
//
//	public void startActivity(){
//		Intent intent = new Intent(getContext(), TestActivity.class);
//		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
//	}
//
//	public void jsmethod_scanning(UZModuleContext moduleContext){
//		mJsCallback = moduleContext;
//		Intent intent = new Intent(getContext(), TestActivity.class);
//		if (!CommonUtil.isBlank(master)) {
//			startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
//		}else {
//			Toast.makeText(mContext, "未登录", Toast.LENGTH_SHORT).show();
//		}
//	}
//
//	@Override
//	protected void onClean() {
//		Log.d(TAG, "onClean: ");
//		if(null != mJsCallback){
//			mJsCallback = null;
//		}
//	}
//}
