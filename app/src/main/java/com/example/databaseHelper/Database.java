package com.example.databaseHelper;

import java.net.URI;
import java.util.Date;

public class Database {
    public final static String DATABASE_NAME = "History.db";
    public final static int DATABASE_VERSION = 1;

    public final static String TABLE_NAME = "history";
    public final static String ID = "id";
    public final static String URI = "URI";
    public final static String DATA = "Data";
    public final static String TEXT = "Text";

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private  Integer id;
    private  String uri;         //图片的存储url
    private  Date data;          //识别时间
    private  String text;        //识别结果


}
