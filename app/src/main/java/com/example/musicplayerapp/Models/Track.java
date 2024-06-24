package com.example.musicplayerapp.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "track_table")
public class Track {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String artist;
    private String filePath;
    private int imageResId; // New field for image resource ID

    public Track(String title, String artist, String filePath, int imageResId) {
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.imageResId = imageResId;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
