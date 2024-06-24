package com.example.musicplayerapp.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.musicplayerapp.Models.Track;

import java.util.List;

@Dao
public interface TrackDao {
    @Insert
    void insert(Track track);

    @Query("SELECT * FROM track_table")
    List<Track> getAllTracks();

    @Query("DELETE FROM track_table")
    void deleteAll();
}
