package com.ostap.prog4app;

import java.io.Serializable;

public class File implements Serializable {
    private String file_name;
    private String url;
    private String user;
    private long size;
    private String date;

    public File(String file_name, String url, String user, long size, String date) {
        this.file_name = file_name;
        this.url = url;
        this.user = user;
        this.size = size;
        this.date = date;
    }

    public File(){

    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
