package com.third.zoom.ytbus.bean;

public class Info {
    private String title;
    private String artist;
    private String duration;
    private String Url;
    private int abulm_id;
    private String path;
    /*    private long size;*/
    public Info(String path) {
        this.path = path;
    }
    public Info() {
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public int getAbulm_id() {
        return abulm_id;
    }

    public void setAbulm_id(int abulm_id) {
        this.abulm_id = abulm_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
