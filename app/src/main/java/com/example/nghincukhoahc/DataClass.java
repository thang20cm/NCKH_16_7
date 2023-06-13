package com.example.nghincukhoahc;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataClass {


    private String dataTitle;
    private String dataDesc;
    private String dataLang;
    private String dataImage;
    private long dateTime;



    private String key;






    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }



    public String getDataTitle() {
        return dataTitle;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public String getDataLang() {
        return dataLang;
    }

    public String getDataImage() {
        return dataImage;
    }
    public long getDateTime() {
        return dateTime;
    }

    public DataClass(String dataTitle, String dataDesc, String dataLang, String dataImage,long dateTime) {

        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.dataLang = dataLang;
        this.dataImage = dataImage;
        this.dateTime = dateTime;

    }
    public DataClass(){

    }
}