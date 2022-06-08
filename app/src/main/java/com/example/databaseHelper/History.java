package com.example.databaseHelper;

import java.io.Serializable;

public class History implements Serializable {
    private Integer id;     // 主键ID
    private String sUri;    //原图Uri

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getsUri() {
        return sUri;
    }

    public void setsUri(String sUri) {
        this.sUri = sUri;
    }

    public String getpUri() {
        return pUri;
    }

    public void setpUri(String pUri) {
        this.pUri = pUri;
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", sUri='" + sUri + '\'' +
                ", pUri='" + pUri + '\'' +
                ", text='" + text + '\'' +
                ", time=" + time +
                '}';
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public History(String sUri, String pUri, String text, Long time) {
        this.sUri = sUri;
        this.pUri = pUri;
        this.text = text;
        this.time = time;
    }

    public History() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String pUri;    // 处理后的Uri
    private String text;     //识别结果
    private Long time;       //时间
}
