package com.example.musicplayerapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.musicplayerapp.Models.Track;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TrackDatabase trackDatabase;
    private List<Track> trackList;
    private int currentTrackIndex = 0;

    private TextView songTitle;
    private TextView artistName;
    private TextView currentTime;
    private TextView totalTime;
    private ImageView albumArt;
    private SeekBar seekBar;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private MusicService musicService;
    private boolean isBound = false;

    private final Handler seekBarHandler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        trackDatabase = TrackDatabase.getDatabase(this);

        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        albumArt = findViewById(R.id.albumArt);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);
        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button backButton = findViewById(R.id.backButton);

        loadTracks();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrack(currentTrackIndex);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    musicService.stopPlayback();
                }
                stopService(new Intent(MainActivity.this, MusicService.class));
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTrackIndex = (currentTrackIndex + 1) % trackList.size();
                playTrack(currentTrackIndex);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTrackIndex = (currentTrackIndex - 1 + trackList.size()) % trackList.size();
                playTrack(currentTrackIndex);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    musicService.seekTo(progress);
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null && musicService.isPlaying()) {
                    int currentPosition = musicService.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    currentTime.setText(formatTime(currentPosition));
                }
                seekBarHandler.postDelayed(this, 1000);
            }
        };
    }

    private void loadTracks() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                trackList = trackDatabase.trackDao().getAllTracks();
                if (!trackList.isEmpty()) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(trackList.get(currentTrackIndex));
                        }
                    });
                } else {
                    Log.d(TAG, "No tracks found in database.");
                }
            }
        });
    }

    private void playTrack(int index) {
        if (index >= 0 && index < trackList.size()) {
            Track track = trackList.get(index);
            Intent startServiceIntent = new Intent(MainActivity.this, MusicService.class);
            startServiceIntent.putExtra("trackFilePath", track.getFilePath());
            startService(startServiceIntent);
            bindService(startServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            updateUI(track);
        }
    }

    private void updateUI(Track track) {
        songTitle.setText(track.getTitle());
        artistName.setText(track.getArtist());
        albumArt.setImageResource(track.getImageResId());
        if (isBound && musicService != null) {
            int duration = musicService.getDuration();
            seekBar.setMax(duration);
            seekBar.setProgress(musicService.getCurrentPosition());
            currentTime.setText(formatTime(musicService.getCurrentPosition()));
            totalTime.setText(formatTime(duration));
            seekBarHandler.post(updateSeekBar);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            if (musicService != null) {
                int duration = musicService.getDuration();
                seekBar.setMax(duration);
                seekBar.setProgress(musicService.getCurrentPosition());
                currentTime.setText(formatTime(musicService.getCurrentPosition()));
                totalTime.setText(formatTime(duration));
                seekBarHandler.post(updateSeekBar);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private String formatTime(int milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }
}
