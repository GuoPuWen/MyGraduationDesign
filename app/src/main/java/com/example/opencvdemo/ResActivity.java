package com.example.opencvdemo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.FocusFinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databaseHelper.Database;
import com.example.databaseHelper.History;
import com.example.databaseHelper.MyHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ResActivity extends AppCompatActivity {

    private static final String TAG = "ResActivity";

    //显示结果页面 图片控件
    private ImageView oriPicImageView;
    private ImageView grayImageView;
    private ImageView cannyImageView;
    private ImageView swtImageView;
    private ImageView componentImageView;
    private ImageView resultImageView;

    //显示结果页面 文字控件
    private TextView grayEdit;
    private TextView cannyEdit;
    private TextView swtEdit;
    private TextView componentEdit;
    private TextView resultEdit;

    //图片Bitmap文件
    private Bitmap source;
    private Bitmap grayImage;
    private Bitmap cannyImage;
    private Bitmap swtImage;
    private Bitmap componentImage;
    private Bitmap resultImage;

    //最后结果显示控件
    private EditText res;

    private NaturalSceneOCR ocr;

    private String path = "";

    private String text = "";   //最终结果

    private Button copy;


    Handler handler;        // Handler 消息处理
    String prtPath = null;
    private Dialog progressDialog;  //进度条
    private SQLiteDatabase database = null;     //数据库对象

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getSupportActionBar().setTitle("基于Android的文字识别系统");
        initDialog();
        initComponent();


        Intent intent = getIntent();
        Uri uri = (Uri) intent.getParcelableExtra("uri");

        String language = (String) intent.getStringExtra("language");
        int useSwt = (int) intent.getIntExtra("useSwt", 1); //默认使用
        Log.i("useSwt", String.valueOf(useSwt));
        source = BitmapUtils.getBitmapFromUri(uri, this);   //通过Uri获取Bitmap
        path = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        ocr = new NaturalSceneOCR(source, path, language, useSwt);

        if (useSwt == 0) {
            noSWT();
        }

        Log.i("dataPath", path);


        //ProgressDialog pd = ProgressDialog.show(this, "提示", "正在识别中", false, true);

        handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                setBitmap();    //显示
                progressDialog.dismiss();
                History history = new History();    //封装History
                history.setsUri(BitmapUtils.saveBitmap(source, ResActivity.this));
                history.setpUri(prtPath);
                history.setTime(new Date().getTime());
                history.setText(text);
                Log.i(TAG, history.toString());
                insertDatabase(history);
            }
        };
        MyOcr myOcr = new MyOcr();
        myOcr.start();


        //复制到剪切板
        copy.setOnClickListener(v -> {
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", res.getText());
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);

            Toast.makeText(this, "复制成功，请粘贴", Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * 识别完成之后显示
     */
    public void setBitmap() {
        oriPicImageView.setImageBitmap(source);

        grayImageView.setImageBitmap(grayImage);
        cannyImageView.setImageBitmap(cannyImage);
        swtImageView.setImageBitmap(swtImage);
        componentImageView.setImageBitmap(componentImage);
        resultImageView.setImageBitmap(resultImage);
        res.setText(text);
    }

    /**
     * ocr 线程类
     */
    class MyOcr extends Thread {
        @Override
        public void run() {
            text = ocr.TesseractOCR();      //获取结果
            grayImage = ocr.getGrayImage(); // 获取swt处理后的图片
            cannyImage = ocr.getEdgeImage();
            swtImage = ocr.getSwtImage();
            componentImage = ocr.getComponents();
            resultImage = ocr.getResult();

            prtPath = BitmapUtils.saveBitmap(resultImage, ResActivity.this);
            Log.i(TAG, "识别完成");
            handler.sendEmptyMessage(0);
        }

    }

    /**
     * 不需要经过SWT处理 控件显示
     */
    public void noSWT() {
        grayImageView.setVisibility(View.GONE);
        cannyImageView.setVisibility(View.GONE);
        swtImageView.setVisibility(View.GONE);
        componentImageView.setVisibility(View.GONE);
        grayEdit.setVisibility(View.GONE);
        cannyEdit.setVisibility(View.GONE);
        swtEdit.setVisibility(View.GONE);
        componentEdit.setVisibility(View.GONE);
        resultEdit.setText("没有经过预处理图片");
    }

    /**
     * 初始化组件库
     */
    public void initComponent() {
        oriPicImageView = findViewById(R.id.ori_pic);
        grayImageView = findViewById(R.id.gray);
        cannyImageView = findViewById(R.id.canny);
        swtImageView = findViewById(R.id.swt);
        componentImageView = findViewById(R.id.component);
        resultImageView = findViewById(R.id.result);

        grayEdit = findViewById(R.id.grayEdit);
        cannyEdit = findViewById(R.id.cannyEdit);
        swtEdit = findViewById(R.id.swtEdit);
        componentEdit = findViewById(R.id.componentEdit);
        resultEdit = findViewById(R.id.resultEdit);

        res = findViewById(R.id.res);
        copy = findViewById(R.id.copy);
    }


    /**
     * 初始化对话框 多线程设置
     */
    public void initDialog() {
        progressDialog = new Dialog(ResActivity.this, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.dialog);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("卖力识别中");
        progressDialog.show();
    }

    /**
     * 插入数据
     * @param data
     */
    public void insertDatabase(History data) {
        MyHelper myHelper = new MyHelper(this);
        database = myHelper.getWritableDatabase();
        ContentValues cV = new ContentValues();
        cV.put(Database.SURI, data.getsUri());
        cV.put(Database.PURI, data.getpUri());
        cV.put(Database.TEXT, data.getText());
        cV.put(Database.TIME, data.getTime().toString());
        database.insert(Database.TABLE_NAME, null, cV);
    }


}



