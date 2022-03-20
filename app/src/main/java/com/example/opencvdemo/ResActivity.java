package com.example.opencvdemo;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ResActivity  extends AppCompatActivity {

    private ImageView ori_pic;
    private ImageView pre_pic;
    private EditText res;

    private Bitmap source;

    private NaturalSceneOCR ocr ;



    private String path = "";

    private String text = "";   //最终结果

    private Bitmap swtImage ;

    private Button copy;
    private static final String TAG = "ResActivity";

    Handler handler;        // Handler 消息处理



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ori_pic = findViewById(R.id.ori_pic);
        pre_pic = findViewById(R.id.pre_pic);
        res = findViewById(R.id.res);
        copy = findViewById(R.id.copy);


        Intent intent = getIntent();
        Uri uri = (Uri)intent.getParcelableExtra("uri");
        String language = (String)intent.getStringExtra("language");
        int useSwt = (int) intent.getIntExtra("useSwt", 1); //默认使用
        Log.i("useSwt", String.valueOf(useSwt));
        try {
            source = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        path = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        ocr = new NaturalSceneOCR(source, path,language,useSwt);
        Log.i("dataPath", path);
        ori_pic.setImageBitmap(source);

        ProgressDialog pd = ProgressDialog.show(this, "提示", "正在识别中", false, true);

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                pre_pic.setImageBitmap(swtImage);
                res.setText(text);
                pd.cancel();
            }
        };

        MyOcr myOcr = new MyOcr();
        myOcr.start();

//        ocr.SWT();
//        new Thread(() -> {
//            text =  ocr.TesseractOCR();
//            Log.i(TAG, "识别完成");
//            swtImage = ocr.getSwtImage();
//            pre_pic.setImageBitmap(swtImage);
//            res.setText(text);
//        }).start();

        //String text = ocr.TesseractOCR();           //大任务 多线程 TODO




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


    class MyOcr extends Thread {
        @Override
        public void run() {

            text =  ocr.TesseractOCR();
            swtImage = ocr.getSwtImage();
            Log.i(TAG, "识别完成");
            handler.sendEmptyMessage(0);
        }

    }

    public void initPd(){

    }
}



