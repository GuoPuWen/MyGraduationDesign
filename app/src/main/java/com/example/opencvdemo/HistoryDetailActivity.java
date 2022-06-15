package com.example.opencvdemo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.databaseHelper.History;

import java.io.File;
import java.io.FileNotFoundException;

public class HistoryDetailActivity extends AppCompatActivity {


    private ImageView ori_pic;
    private ImageView pre_pic;
    private EditText res;
    private Button copy;
    private final String TAG = "HistoryDetailActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        getSupportActionBar().setTitle("基于Android的文字识别系统");

        ori_pic = findViewById(R.id.ori_pic);
        pre_pic = findViewById(R.id.pre_pic);
        res = findViewById(R.id.res);

        copy = findViewById(R.id.copy);

        Intent intent = getIntent();
//        Bundle bundle = intent.getExtras();
        History data = (History)intent.getSerializableExtra("data");
        Log.i(TAG, data.toString());
//        String TargetPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "images";
        ori_pic.setImageBitmap(BitmapUtils.getBitmapFromPath(data.getsUri()));
        pre_pic.setImageBitmap(BitmapUtils.getBitmapFromPath(data.getpUri()));
        res.setText(data.getText());


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
    public Bitmap initBitmap(String uri) throws FileNotFoundException {
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(uri)));
    }



}
