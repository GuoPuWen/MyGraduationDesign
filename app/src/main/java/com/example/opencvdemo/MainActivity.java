package com.example.opencvdemo;


import android.os.Bundle;

import android.app.Activity;

import android.content.ContentValues;
import android.content.Intent;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.databaseHelper.MyHelper;
import com.sl.utakephoto.compress.CompressConfig;
import com.sl.utakephoto.crop.CropOptions;
import com.sl.utakephoto.exception.TakeException;
import com.sl.utakephoto.manager.ITakePhotoResult;
import com.sl.utakephoto.manager.TakePhotoManager;
import com.sl.utakephoto.manager.UTakePhoto;


import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String language = "eng";
    private int useSwt;     //是否使用swt

    private TextView cropText;              //x 或者 /
    private Button start;                 //Start
    private RadioGroup type_group;          //选择拍照或者选择照片Radio组
    private RadioGroup crop;                //选择裁剪 Radio组
    private RadioGroup cropRadioGroup;      //选择尺寸或者比例 Radio组
    private RadioGroup comprssRadioGroup;   //选择是否压缩
    private RadioGroup rotateRadioGroup;    //选择是否选择处理
    private EditText outputX;           //宽
    private EditText outputY;           //宽
    private RadioGroup ocrTypeRadioGroup;   //选择中文或者英文
    private RadioGroup swtRadioGroup;       //是否使用swt算法
    private ImageView photoIv;          //最后显示图片

    private Button ocr;

    private Uri uri = null;



    private RadioButton take_photo_btn;     // 拍照

    CropOptions.Builder cropBuilder;             //裁剪内部类
    TakePhotoManager photoManager;           //管理类

    public  void init() {
        type_group = findViewById(R.id.type_group);
        cropText = findViewById(R.id.cropText);
        crop = findViewById(R.id.crop);
        cropRadioGroup = findViewById(R.id.cropRadioGroup);
        comprssRadioGroup = findViewById(R.id.comprssRadioGroup);
        rotateRadioGroup = findViewById(R.id.rotateRadioGroup);
        ocrTypeRadioGroup = findViewById(R.id.ocrTypeRadioGroup);
        swtRadioGroup = findViewById(R.id.swtRadioGroup);

        outputX = findViewById(R.id.outputX);
        outputY = findViewById(R.id.outputY);

        start = findViewById(R.id.capture);

        photoIv = findViewById(R.id.photoIv);

        photoManager = UTakePhoto.with(this);
        cropBuilder = new CropOptions.Builder();    //裁剪选项

        ocr = findViewById(R.id.ocr);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode == Activity.RESULT_OK){
            Toast.makeText(this,"授权成功",Toast.LENGTH_SHORT).show();
        }
    }

    //该方法用于创建显示Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_optionmenu,menu);
        return true;
    }

    //该方法对菜单的item进行监听
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                Toast.makeText(this, "点击了第" + 1 + "个", Toast.LENGTH_SHORT).show();

                break;
            case R.id.menu2:
                Toast.makeText(this, "点击了第" + 2 + "个", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        createDatabase();           //初始化数据库adb

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
        //选择ocr识别语言
        ocrTypeRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.chinese :
                    language = "chi_sim";
                    Log.i("language", language);
                    break;
                case R.id.eng:
                    language = "eng";
                    Log.i("language", language);
                    break;
                default:
                    Log.e(TAG, "ocrTypeRadioGroup is error");
                    break;
            }
        });
        //是否使用swt算法
        swtRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.useSwt :
                    useSwt = 1;
                    Log.i("useSwt", String.valueOf(useSwt));
                    break;
                case R.id.noUseSwt:
                    useSwt = 0;
                    Log.i("useSwt", String.valueOf(useSwt));
                    break;
                default:
                    Log.e(TAG, "ocrTypeRadioGroup is error");
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
                        uri = uriList.get(0);
                        photoIv.setImageURI(uri);
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


        ocr.setOnClickListener((v) -> {
            if(uri != null) {
                Intent intent = new Intent(MainActivity.this, ResActivity.class);
                intent.putExtra("uri",uri);
                intent.putExtra("useSwt", useSwt );  //1 是使用 0不使用
                intent.putExtra("language", language);
                startActivity(intent);
            }
        });


    }

    public void createDatabase() {
        MyHelper myHelper = new MyHelper(this);
        myHelper.getWritableDatabase();
    }
}