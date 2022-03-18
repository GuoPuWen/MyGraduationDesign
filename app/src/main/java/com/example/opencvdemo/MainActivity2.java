package com.example.opencvdemo;

import android.content.ContentResolver;
import android.os.Bundle;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.sl.utakephoto.compress.CompressConfig;
import com.sl.utakephoto.crop.CropOptions;
import com.sl.utakephoto.exception.TakeException;
import com.sl.utakephoto.manager.ITakePhotoResult;
import com.sl.utakephoto.manager.TakePhotoManager;
import com.sl.utakephoto.manager.UTakePhoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView cropText;              //x 或者 /
    private Button start;                 //Start
    private RadioGroup type_group;          //选择拍照或者选择照片Radio组
    private RadioGroup crop;                //选择裁剪 Radio组
    private RadioGroup cropRadioGroup;      //选择尺寸或者比例 Radio组
    private RadioGroup comprssRadioGroup;   //选择是否压缩
    private RadioGroup rotateRadioGroup;    //选择是否选择处理
    private EditText outputX;           //宽
    private EditText outputY;           //宽
    private ImageView photoIv;          //最后显示图片



    private RadioButton take_photo_btn;     // 拍照

    CropOptions.Builder cropBuilder;             //裁剪内部类
    TakePhotoManager photoManager;           //管理类

    public  void init() {
        type_group = findViewById(R.id.type_group);
        crop = findViewById(R.id.crop);
        cropRadioGroup = findViewById(R.id.cropRadioGroup);
        comprssRadioGroup = findViewById(R.id.comprssRadioGroup);
        rotateRadioGroup = findViewById(R.id.rotateRadioGroup);

        outputX = findViewById(R.id.outputX);
        outputY = findViewById(R.id.outputY);

        start = findViewById(R.id.capture);

        photoIv = findViewById(R.id.photoIv);

        photoManager = UTakePhoto.with(this);
        cropBuilder = new CropOptions.Builder();    //裁剪选项
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode == Activity.RESULT_OK){
            Toast.makeText(this,"授权成功",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //拍照或者选择照片
        type_group.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.take_photo_btn:
                    photoManager.openCamera("Pictures/uTakePhoto");
                    break;
                case R.id.selelct_photo_btn:
                    photoManager.openAlbum();
                    break;
                default:
                    Log.e(TAG, "type_group is error");
                    break;
            }
        });
        //裁剪选项
        crop.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.noCropBtn :
                    cropBuilder = null;
                    break;
                case R.id.system_crop_btn :
                    cropBuilder = new CropOptions.Builder();
                    cropBuilder.setWithOwnCrop(false);
                    break;
                case R.id.own_crop_btn :
                    cropBuilder = new CropOptions.Builder();
                    cropBuilder.setWithOwnCrop(true);
                    break;
                default:
                    Log.e(TAG, "cropBuilder is error");
                    break;
            }
        });

        cropRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.outputBtn:
                    cropText.setText("*");
                    if(cropBuilder != null) {
                        cropBuilder.setOutputX(Integer.parseInt(outputX.getText().toString()));
                        cropBuilder.setOutputY(Integer.parseInt(outputY.getText().toString()));
                    }
                    break;
                case R.id.aspectBtn :
                    cropText.setText("/");
                    if(cropBuilder != null) {
                        cropBuilder.setAspectX(Integer.parseInt(outputX.getText().toString()));
                        cropBuilder.setAspectY(Integer.parseInt(outputY.getText().toString()));
                    }
                    break;
                default:
                    Log.e(TAG, "cropRadioGroup is error");
                    break;
            }
        });
        //是否压缩
        comprssRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.compress :
                    photoManager.setCompressConfig(
                            new CompressConfig.Builder().setLeastCompressSize(50).setTargetUri(
                                    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues())
                            ).create()
                    );       //旋转
                    break;
                case R.id.noCompress:
                    photoManager.setCompressConfig(null);       //不旋转
                    break;
                default:
                    Log.e(TAG, "comprssRadioGroup is error");
                    break;
            }
        });
        //是否旋转
        rotateRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.rotateProcessing :
                    photoManager.setCameraPhotoRotate(true);       //旋转
                    break;
                case R.id.noRotateProcessing:
                    photoManager.setCameraPhotoRotate(false);       //不旋转
                    break;
                default:
                    Log.e(TAG, "rotateRadioGroup is error");
                    break;
            }
        });
        //
        start.setOnClickListener((View v) -> {
            if(cropBuilder != null && (TextUtils.isEmpty(outputX.getText()) || TextUtils.isEmpty(outputY.getText()))) {
                Toast.makeText(this, "请输入宽高", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "请输入宽高");
                return;
            }
            if(cropBuilder != null) {
                photoManager.setCrop(cropBuilder.create());
            }else{
                photoManager.setCrop(null);
            }

            photoManager.build(new ITakePhotoResult() {
                @Override
                public void takeSuccess(List<Uri> uriList) {
                    if (uriList != null) {
                        photoIv.setImageURI(uriList.get(0));
                    }
                }

                @Override
                public void takeFailure(TakeException ex) {
                    if(ex != null) {
                        Log.i("photoManager Result", ex.getMessage());
                    }
                }

                @Override
                public void takeCancel() {

                }
            });
        });



    }
}