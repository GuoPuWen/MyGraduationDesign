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
    public NaturalSceneOCR(Bitmap source){
        this.oriImage = source;

        int width = source.getWidth();
        int height = source.getHeight();
        swtImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    }

    /**
     * 加载native-lib类
     */
    static {
        System.loadLibrary("native-lib");
    }

    private final static String TAG = "NaturalSceneOCR";     //全局日志TAG

    private Bitmap oriImage;        //原始图像
    private Bitmap swtImage;        //经过SWT处理之后的图像
    private String res;             //经过Tesseract OCR处理得到的结果
    private final static String LANGUAGETYPE = "eng";           //Tesseract识别的语言类型
    private final static String PATH = "";                         //Tesseract语言包地址



    /**
     * 使用Tesseract进行识别
     * @param lan     语言类型 eng为英语
     * @return  返回识别结果
     */
    public String TesseractOCR(String lan) {
        SWT();
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(PATH, lan);
        tessBaseAPI.setImage(swtImage);                     //传入SWT处理之后的图像
        String text = tessBaseAPI.getUTF8Text();            //得到结果
        Log.i(TAG, text);
        return text;
    }

    /**
     * SWT算法 调用本地方法
     */
    public void SWT() {
        JniBitmapUseSWT(oriImage, swtImage);
    }

    /**
     * 调用Native本地方法，进行SWT预处理
     * @param source 原始Bitmap
     * @param res 经过SWT算法处理之后的Bitmap
     */
    public native void JniBitmapUseSWT(Bitmap source, Bitmap res);

}
