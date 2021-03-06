package com.example.opencvdemo;

import android.graphics.Bitmap;
import android.icu.text.BidiRun;
import android.icu.text.RelativeDateTimeFormatter;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import javax.xml.transform.Source;

/**
 * 自然场景下的OCR识别
 */
public class NaturalSceneOCR {

    /**
     * 构造方法
     * @param source
     */
    public NaturalSceneOCR(Bitmap source, String path, String language, int useSwt){
        this.oriImage = source;

        int width = source.getWidth();
        int height = source.getHeight();
        grayImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        edgeImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        saveSWT = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        components = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.path = path;
        this.language = language;
        this.useSwt = useSwt;
    }

    /**
     * 加载native-lib类
     */
    static {
        System.loadLibrary("native-lib");
    }

    private final static String TAG = "NaturalSceneOCR";     //全局日志TAG

    private Bitmap oriImage;        //原始图像
    private String res;             //经过Tesseract OCR处理得到的结果
    private static String language ;           //Tesseract识别的语言类型     //T
    private String path = "";
    // esseract语言包地址
    private int useSwt; //是否使用SWT


    Bitmap grayImage;
    Bitmap edgeImage;
    Bitmap saveSWT;

    public Bitmap getGrayImage() {
        return grayImage;
    }

    public Bitmap getEdgeImage() {
        return edgeImage;
    }

    public Bitmap getSaveSWT() {
        return saveSWT;
    }

    public Bitmap getComponents() {
        return components;
    }

    public Bitmap getResult() {
        return result;
    }

    Bitmap components;
    Bitmap result;

    public Bitmap getSwtImage() {
        return saveSWT;
    }





    /**
     * 使用Tesseract进行识别
     * @param       eng为英语
     * @return  返回识别结果
     */
    public String TesseractOCR() {
        if(useSwt == 1) {
            SWT();
        }else{
            result = oriImage;
        }

        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(path, language);
        tessBaseAPI.setImage(result);                     //传入SWT处理之后的图像
        String text = tessBaseAPI.getUTF8Text();            //得到结果
        Log.i(TAG, text);
        return text;
    }

    /**
     * SWT算法 调用本地方法
     */
    public void SWT() {
        JniBitmapUseSWT(oriImage, grayImage, edgeImage, saveSWT, components, result);
    }

    /**
     * 调用Native本地方法，进行SWT预处理
     * @param source 原始Bitmap
     * @param res 经过SWT算法处理之后的Bitmap
     */
    public native void JniBitmapUseSWT(Bitmap source,
                                       Bitmap grayImage,
                                       Bitmap edgeImage,
                                       Bitmap saveSWT,
                                       Bitmap components,
                                       Bitmap result);

}
