package com.example.musicplayerapp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.musicplayerapp.DAO.TrackDao;
import com.example.musicplayerapp.Models.Track;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Database(entities = {Track.class}, version = 1, exportSchema = false)
public abstract class TrackDatabase extends RoomDatabase {

    public abstract TrackDao trackDao();

    private static volatile TrackDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static TrackDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TrackDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TrackDatabase.class, "track_database")
                            .fallbackToDestructiveMigration() // Clears old data and resets the database schema
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        try {
                                            Future<Void> result = populateDatabase(context);
                                            result.get(); // Ensure data insertion completes
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static Future<Void> populateDatabase(final Context context) {
        return databaseWriteExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() {
                TrackDao dao = INSTANCE.trackDao();
                dao.deleteAll();

                // Insert tracks with image resource IDs
                Track track1 = new Track(
                        "Wanderlust (WILDLYF Remix)",
                        "blackbear",
                        "android.resource://" + context.getPackageName() + "/" + R.raw.sample_music,
                        R.drawable.album_art // Replace with actual drawable resource ID
                );

                Track track2 = new Track(
                        "fashion week (it's different remix)",
                        "blackbear",
                        "android.resource://" + context.getPackageName() + "/" + R.raw.sample_music1,
                        R.drawable.album_art1
                );

                dao.insert(track1);
                dao.insert(track2);
                // Add more tracks if needed
                // Track track2 = new Track(...);
                // dao.insert(track2);

                return null;
            }
        });
    }
}




