package com.example.dean;

import java.io.Serializable;

public class music implements Serializable {
    private String resourceId;
    private String musicTitle;
    private String artist;
    private long musicLength;
    private String filePath;

    private String uriFilePath;
    private String albumArtBitmap;

    public String getAlbumArtName() {
        return albumArtName;
    }

    public void setAlbumArtName(String albumArtName) {
        this.albumArtName = albumArtName;
    }

    private String albumArtName;

    public String getFilePath() {
        return filePath;
    }

    public String getUriFilePath() { return uriFilePath; }
    public void setUriFilePath(String uriFilePath) {this.uriFilePath = uriFilePath;}

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public music(String resourceId, String musicTitle, String artist, long musicLength, String filePath, String albumArtBitmap, String uriFilePath, String albumArtName){
        this.resourceId = resourceId;
        this.musicTitle = musicTitle;
        this.artist = artist;
        this.musicLength = musicLength;
        this.filePath = filePath;
        this.albumArtBitmap = albumArtBitmap;
        this.uriFilePath = uriFilePath;
        this.albumArtName = albumArtName;
    }
    public music(){}

    public String getAlbumArtBitmap() {
        return albumArtBitmap;
    }
    public void setAlbumArtBitmap(String albumArtBitmap) {this.albumArtBitmap = albumArtBitmap; }
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
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

    public void setMusicLength(long musicLength) {
        this.musicLength = musicLength;
    }
}