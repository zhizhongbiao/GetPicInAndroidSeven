package cn.com.tianyudg.getpicinandroidseven;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {


    private static final int CODE_TAKE_PIC = 122;
    private static final int CODE_GET_PIC = 123;
    private static final String TAG = "MainActivity";
    private static final int CODE_CROP_IMAGE = 143;
    private File imageFile;
    private Uri imageUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.iv);
        MainActivityPermissionsDispatcher.onAllowedWithCheck(this);
    }

    public void getPic(View view) {
        pickImageFromGallery();
    }

    public void takePic(View view) {
        imageUri = getUriFromFilePath();
        takeImageFromCamera(imageUri);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            Log.e(TAG, "onActivityResult: uri/requestCode=" + uri + "/" + requestCode);
        } else {
            Log.e(TAG, "data == null------requestCode=" + requestCode);
        }


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODE_GET_PIC:


                    if (data != null) {
                        Uri uri = data.getData();
                        Log.e(TAG, "RESULT_OK: uri/requestCode=" + uri + "/" + requestCode);
                    } else {
                        Log.e(TAG, "RESULT_OK  data == null------requestCode=" + requestCode);
                    }
                    PickImageUtil.cropImage(this, data.getData(), CODE_CROP_IMAGE);
                    break;
                case CODE_TAKE_PIC:

                    if (data != null) {
                        Uri uri = data.getData();
                        Log.e(TAG, "RESULT_OK: uri/requestCode=" + uri + "/" + requestCode);
                    } else {
                        Log.e(TAG, "RESULT_OK  data == null------requestCode=" + requestCode);
                    }
                    PickImageUtil.cropImage(this, imageUri, CODE_CROP_IMAGE);
                    break;
                case CODE_CROP_IMAGE:

                    if (data != null) {
                        Uri uri = data.getData();
                        Log.e(TAG, "RESULT_OK: uri/requestCode=" + uri + "/" + requestCode);
                    } else {
                        Log.e(TAG, "RESULT_OK  data == null------requestCode=" + requestCode);
                    }
                    setPicToView(data);
                    break;
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 相机拍照,然后指定路径保存下来
     */
    private void takeImageFromCamera(Uri uri) {

        if (uri == null) return;

        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //只是保存在指定的存储位置,并没有返回给onActivityResult方法中
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(captureIntent, CODE_TAKE_PIC);
    }

    private Uri getUriFromFilePath() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File testTempDirectory = new File(rootPath + File.separator + "TestTemp");
        boolean isFileCreated = true;
        if (!testTempDirectory.exists()) {
            isFileCreated = testTempDirectory.mkdirs();
        }

        if (!isFileCreated) {
            Log.e(TAG, "takeImageFromCamera: 头像文件夹创建不成功!");
            return null;
        }

        File imageFile;
        try {
            imageFile = File.createTempFile("Avatar_" + System.currentTimeMillis()  /* prefix */
                    , ".jpg"         /* suffix */
                    , testTempDirectory   /* directory */);
        } catch (IOException e) {
            Log.e(TAG, "takeImageFromCamera: 系统出错!");
            e.printStackTrace();
            return null;

        }


        Uri imageUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            //Android 7.0 以及以上
            imageUri = FileProvider.getUriForFile(this, "cn.com.tianyudg.getpicinandroidseven", imageFile);
        } else {
            //Android 7.0 以下
            imageUri = Uri.fromFile(imageFile);
        }

        LogUtils.e("imageFile=" + imageFile.getAbsolutePath().toString() + "  -----------  imageUri=" + imageUri.toString());

        return imageUri;

    }


    /**
     * 充系统相册中获取图片
     */
    public void pickImageFromGallery() {
        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CODE_GET_PIC);

    }


    /**
     * 显示裁剪之后的图片
     *
     * @param picData
     */
    private void setPicToView(Intent picData) {
        Bundle extras = picData.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            imageView.setImageBitmap(photo);
        }
    }


    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onAllowed() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onDenied() {
        Toast.makeText(this, "权限拒绝", Toast.LENGTH_SHORT);
        finish();
    }
}
