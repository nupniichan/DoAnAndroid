package com.example.dean;

public class music {
    private int resourceId;
    private String musicTitle;
    private String artist;
    private long musicLength;


    public music(int resourceId, String musicTitle, String artist, long musicLength){
        this.resourceId = resourceId;
        this.musicTitle = musicTitle;
        this.artist = artist;
        this.musicLength = musicLength;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getMusicLength() {
        return (int) musicLength;
    }

    public void setMusicLength(int musicLength) {
        this.musicLength = musicLength;
    }
}