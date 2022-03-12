package com.example.opencvdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Array;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ImageView imgSrc;
    private ImageView imgRes;
    private TextView tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgSrc = findViewById(R.id.img_src);
        imgRes = findViewById(R.id.img_res);
        tx = findViewById(R.id.fab);

        Bitmap source = loadBitmap();
        imgSrc.setImageBitmap(source);

        int width = source.getWidth();
        int height = source.getHeight();
//        int[] pixel = new int[width * height];
//        source.getPixels(pixel, 0, width, 0, 0, width, height);
//        Log.i("Source Pixel", Arrays.toString(pixel));

//        int[] grayPixel = doGray(pixel, width, height);

        Bitmap res = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        JniBitmapUseSWT(source, res);
//        Bitmap grayRes = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        grayRes.setPixels(grayPixel, 0, width, 0, 0, width, height);
        saveBitmap("res.jpg", res, this);
        imgRes.setImageBitmap(res);

    }

    /**
     * 加载图片为Bitmap
     * @return
     */
    public Bitmap loadBitmap(){

        String path = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "11.png";
        Log.i("路径", path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        Log.i("bitmap信息", String.valueOf(bitmap.getWidth()));
        return bitmap;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public native void JniBitmapUseSWT(Bitmap source, Bitmap res);

    public void saveBitmap(String name, Bitmap bm, Context mContext) {
        Log.d("Save Bitmap", "Ready to save picture");
        //指定我们想要存储文件的地址
        String TargetPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath();
        Log.d("Save Bitmap", "Save Path=" + TargetPath);
        //判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);

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
    }

    static boolean fileIsExist(String fileName)
    {
        //传入指定的路径，然后判断路径是否存在
        File file=new File(fileName);
        if (file.exists())
            return true;
        else{
            //file.mkdirs() 创建文件夹的意思
            return file.mkdirs();
        }
    }
}