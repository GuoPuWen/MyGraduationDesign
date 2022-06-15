package com.example.opencvdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BitmapUtils {


    private static final String TAG = "BitmapUtils";

    static String saveBitmap( Bitmap bm, Context mContext) {

        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyyMMddHHmmssSSS");
        //获取当前时间并作为时间戳
        String filename = simpleDateFormat.format(new Date()) + ".jpg";


        Log.d("Save Bitmap", "Ready to save picture");
        //指定我们想要存储文件的地址
//        String TargetPath = mContext.getFilesDir() + "/images/";

        String TargetPath = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "images";
        Log.d("Save Bitmap", "Save Path=" + TargetPath);
        //判断指定文件夹的路径是否存在
        if (!SDUtils.fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, filename);

            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture is save to your phone!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        Log.i(TAG, filename);
        return TargetPath + File.separator + filename;
    }

    public static Bitmap getBitmapFromPath(String path) {
        if (!new File(path).exists()) {
            Log.i(TAG, "文件不存在");
            return null;
        }
        byte[] buf = new byte[1024 * 1024];// 1M
        Bitmap bitmap = null;

        try {
            FileInputStream fis = new FileInputStream(path);
            int len = fis.read(buf, 0, buf.length);
            bitmap = BitmapFactory.decodeByteArray(buf, 0, len);
            if (bitmap == null) {
                System.out.println("len= " + len);
                System.err
                        .println("path: " + path + "  could not be decode!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return bitmap;
    }

    public static Bitmap getBitmapFromUri(Uri uri, Context mContext) {
        try {
            return  BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
