package com.websocketim.activity;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.websocketim.R;
import com.websocketim.manager.DialogManager;
import com.websocketim.utils.FileSizeUtil;
import com.websocketim.utils.FileUtils;
import com.websocketim.utils.ToastUtils;


public class SettingsActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "SettingsActivity";

    private ImageView back;
    private TextView filesize;
    private View cache;
    private View version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.back = (ImageView) findViewById(R.id.back);
        this.filesize = (TextView) findViewById(R.id.filesize);
        this.cache = findViewById(R.id.cache);
        this.version = findViewById(R.id.version);

        this.back.setOnClickListener(this);
        this.cache.setOnClickListener(this);
        this.version.setOnClickListener(this);

        double fileSize = FileSizeUtil.getFileOrFilesSize(FileUtils.getPath(),3);
        this.filesize.setText(fileSize+"MB");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.cache:
                DialogManager.showDialog(SettingsActivity.this, "\n确定清理缓存？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FileUtils.clearFile();
                        double fileSize = FileSizeUtil.getFileOrFilesSize(FileUtils.getPath(),3);
                        filesize.setText(fileSize+"MB");
                        ToastUtils.showShort(getApplication(),"清理完成");
                    }
                },null,null);
                break;
            case R.id.version:
                PackageInfo pi = null;
                try {
                    pi = getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
                   String version = pi.versionName;
                    ToastUtils.showShort(getApplication(),"当前版本："+version);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
