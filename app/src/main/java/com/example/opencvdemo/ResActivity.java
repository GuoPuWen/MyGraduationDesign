package com.example.opencvdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;

public class ResActivity  extends AppCompatActivity {

    private ImageView ori_pic;
    private ImageView pre_pic;
    private TextView res;

    private Bitmap source;

    private NaturalSceneOCR ocr ;

    private String path = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ori_pic = findViewById(R.id.ori_pic);
        pre_pic = findViewById(R.id.pre_pic);
        res = findViewById(R.id.res);


        Intent intent = getIntent();
        Uri uri = (Uri)intent.getParcelableExtra("uri");

        try {
            source = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        path = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        ocr = new NaturalSceneOCR(source, path);
        Log.i("dataPath", path);
        ori_pic.setImageBitmap(source);

        ocr.SWT();
        Bitmap swtImage = ocr.getSwtImage();
        pre_pic.setImageBitmap(swtImage);

        String text = ocr.TesseractOCR("eng");
        res.setText(text);

    }


}

