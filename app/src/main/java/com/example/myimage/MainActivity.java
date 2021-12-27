package com.example.myimage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private ImageView iv_show_select;
    private int CAMERA_REQ_CODE = 1;     //调用相机成功返回码
    private int ALBUM_REQ_CODE = 2;     //调用相册成功返回码
    private int CROP_REQ_CODE = 3;      //调用图片裁剪成功返回码
    private Uri uri;                    //调用裁剪之后图片保存的uri
    private Uri uriTemp;                //调用相册或者相机之后的Uri



    private File imageFile;             // 保存图片的文件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_show_select = findViewById(R.id.iv_show_select);


    }
    //绑定事件
    public void camera(View v){
        //Android 6版本以上需要
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            openCamera();
        }

    }
    //绑定事件
    public void album(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, ALBUM_REQ_CODE);
    }



    public void startOcr(View v){
        Intent intent = new Intent(this, OcrActivity.class);
        startActivity(intent);
    }




    /**
     * 拍照方法
     */
    public void openCamera(){

        initImageFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            uriTemp = FileProvider.getUriForFile(MainActivity.this, "com.example.myimage.fileprovider", imageFile);
            Log.i("版本号", Build.VERSION.SDK_INT + "");
        }else{
            uriTemp = Uri.fromFile(imageFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTemp);
        startActivityForResult(intent, CAMERA_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK){
//            Log.i("camera-", data.toString());
//            if(data != null && data.hasExtra("data")){
//                iv_show_select.setImageBitmap(data.getParcelableExtra("data"));
//                Log.i("camera-", "不使用uri");
//            }else{
//                try {
//                    InputStream is = getContentResolver().openInputStream(uri);
//                    iv_show_select.setImageBitmap(BitmapFactory.decodeStream(is));
//                    is.close();
//                } catch (Exception e) {
//                    Log.i("camera-", e.getMessage());
//                    e.printStackTrace();
//                }
//                Log.i("camera-", "使用uri");
//                Log.i("camera-uri", uri.toString());
//            }
            Log.i("camera-uriTemp 拍照之后的Uri路径", uriTemp.getPath());
            openCrop(uriTemp);

        }else if(requestCode == CAMERA_REQ_CODE && resultCode == RESULT_CANCELED){
            Log.i("camera-", "拍照取消");
        }else if(requestCode == ALBUM_REQ_CODE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                iv_show_select.setImageBitmap(bitmap);
            } catch (Exception e) {

            }
            Log.i("album-", "选择相册");
        }else if(requestCode == CROP_REQ_CODE && resultCode == RESULT_OK){
            if(data != null && data.hasExtra("data")){
                iv_show_select.setImageBitmap(data.getParcelableExtra("data"));
                Log.i("camera-", "不使用uri");
            }else{
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    iv_show_select.setImageBitmap(BitmapFactory.decodeStream(is));
                    is.close();
                } catch (Exception e) {
                    Log.i("camera-", e.getMessage());
                    e.printStackTrace();
                }
                Log.i("camera-uri显示界面的路径", uri.getPath());
            }
        }

    }


    /**
     * 判断是否有SD卡
     *
     * @return 有SD卡返回true，否则false
     */
    private boolean hasSDCard() {
        // 获取外部存储的状态
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // 有SD卡
            return true;
        }
        return false;

    }


    /**
     * 初始化存储图片的文件
     *
     * @return 初始化成功返回true，否则false
     */
    private void initImageFile() {
    // 有SD卡时才初始化文件

        // 构造存储图片的文件的路径，文件名为当前时间
        String filename = System.currentTimeMillis()  + ".png";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File folder = new File(dir,"my_pictures");
        if(!folder.exists()){
            //创建目录
            folder.mkdir();
        }
        imageFile=new File(folder,filename);
    }

    /**
     * uri是上一个Intent传入的图片Uri
     * @param uriImage
     */
    private void cropPhoto(Uri uriImage) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //添加读写权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //设置裁剪数据来源和类型
        intent.setDataAndType(uriImage, "image/*");
        //设置裁剪为true
        intent.putExtra("crop", "true");
        //设置大小
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        //不能以这种形式返回图片数据 因为现在图片很大 我们得以 uri 方式返回
        //intent.putExtra("return-data", true); 配合 onActivityResult
        // =》 Bundle bundle = intent.getExtras();
        // =》 final Bitmap image = bundle.getParcelable("data");
        //创建 uri getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) 这个地方会出现 注意点二 或 注意点三 的报错
//        initImageFile();
//
//
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
//            uri = FileProvider.getUriForFile(MainActivity.this, "com.example.myimage.fileprovider", imageFile);
//            Log.i("版本号", Build.VERSION.SDK_INT + "");
//        }else{
//            uri = Uri.fromFile(imageFile);
//        }


        String filename = System.currentTimeMillis()  + ".png";

        uri = Uri.parse("file://" + "/" + getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + filename);

        //设置 uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //设置格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());


       Log.i("裁剪图片之后的路径", uri.getPath());

        startActivityForResult(intent, CROP_REQ_CODE);
    }

    public void openCrop(Uri uri){
        //Android 6版本以上需要
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            cropPhoto(uri);
        }
    }

}