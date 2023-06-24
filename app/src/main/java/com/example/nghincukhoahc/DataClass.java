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
    private String dataFile;
    private long dateTime;



    private String key;






    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
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

    public DataClass(String dataTitle, String dataDesc, String dataLang, String dataImage,String dataFile,long dateTime) {

        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.dataLang = dataLang;
        this.dataImage = dataImage;
        this.dataFile = dataFile;
        this.dateTime = dateTime;

    }
    public DataClass(){

    }
}