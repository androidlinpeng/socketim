package com.websocketim.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.websocketim.Constants;
import com.websocketim.R;
import com.websocketim.model.UserInfo;
import com.websocketim.utils.CommonUtil;
import com.websocketim.utils.FileUtils;
import com.websocketim.utils.GlideLoader;
import com.websocketim.utils.MsgCache;

import java.io.File;

public class PersonalEditActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PersonalEditActivity";
    
    private String avatorUrl = "";
    private ImageView avatar;
    private ImageView back;

    private String master = "";
    private String curPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_edit);

        UserInfo userinfo = (UserInfo) MsgCache.get(PersonalEditActivity.this).getAsObject(Constants.USER_INFO);
        if (!CommonUtil.isBlank(userinfo)) {
            master = userinfo.getUsername();
            avatorUrl = userinfo.getHead50();
        }

        this.back = (ImageView) findViewById(R.id.back);
        this.avatar = (ImageView) findViewById(R.id.avatar);
        GlideLoader.LoderCircleAvatar(getApplication(),avatorUrl,this.avatar);

        this.back.setOnClickListener(this);
        this.avatar.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.avatar:
                AlertDialog.Builder dialog = new AlertDialog.Builder(PersonalEditActivity.this);
                dialog.setTitle("");
                dialog.setItems(R.array.media_list_dialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                intentMedia(RESULT_GALLERY);
                                break;
                            case 1:
                                intentMedia(RESULT_CAMERA);
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                dialog.show();
                break;
        }
    }

    private static final int RESULT_GALLERY = 100;
    private static final int RESULT_CAMERA = 200;

    public void intentMedia(int type) {
        Intent intent;
        if (type == RESULT_GALLERY) {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, RESULT_GALLERY);
        } else if (type == RESULT_CAMERA) {
            String fileName = "_" + System.currentTimeMillis() + "_PIC.jpg";
            File file = FileUtils.createTempFile(fileName);
            if (null != file && file.exists()) {
                curPhotoPath = file.getPath();
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //系统7.0打开相机权限处理
                if (Build.VERSION.SDK_INT >= 24) {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                    Uri uri = getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } else {
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                }
                startActivityForResult(intent, RESULT_CAMERA);
            }
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (arg1 == RESULT_OK) {
            String path = "";
            switch (arg0) {
                case RESULT_GALLERY:
                    if (null != arg2 && null != arg2.getData()) {
                        Uri uri = arg2.getData();
                        uri = CommonUtil.getPictureUri(arg2, PersonalEditActivity.this);
                        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        path = cursor.getString(columnIndex);
                        cursor.close();
                    }
                    break;
                case RESULT_CAMERA:
                    path = curPhotoPath;
                    break;
                default:
                    break;
            }
        }
    }


}

