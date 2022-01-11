package com.example.myimage;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myimage.swt.StrokeWidthTransform;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;

public class OcrActivity extends AppCompatActivity {

    private ImageView ori_pic;
    private ImageView pre_pic;
    private TextView res;
    private Mat sctImg;

    //private String PATH = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "11.jpg";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ori_pic = findViewById(R.id.ori_pic);
        pre_pic = findViewById(R.id.pre_pic);
        res = findViewById(R.id.res);



        Bitmap bitmap = loadBitmap();
        show(ori_pic, bitmap);

        //测试opencv 变为灰度图
        StrokeWidthTransform.init(this);
        Bitmap gray = StrokeWidthTransform.bitmap2Gray(bitmap);
        show(pre_pic, gray);
        //startOcr();
    }




    public void show(ImageView imageView, Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    public Bitmap loadBitmap(){
        String path = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "13.jpg";
        Log.i("路径", path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        Log.i("bitmap信息", String.valueOf(bitmap.getWidth()));
        return bitmap;
    }

    public void startOcr(){
        File dataPath = this.getExternalFilesDir(null);
        String pathName = dataPath.getPath();
        Log.i("dataPath", pathName);
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(pathName, "chi_sim");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
        String picPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator + "11.jpg";

        File file = new File(picPath);

        Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
        tessBaseAPI.setImage(bitmap);
        String text = tessBaseAPI.getUTF8Text();
        Log.i("text", text);

        ori_pic.setImageBitmap(bitmap);
        pre_pic.setImageBitmap(bitmap);
        res.setText(text);
    }
}