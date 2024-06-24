package com.example.musicplayerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayerapp.Models.Track;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final List<Track> trackList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Track track);
    }

    public TrackAdapter(List<Track> trackList, OnItemClickListener listener) {
        this.trackList = trackList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_item, parent, false);
        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = trackList.get(position);
        holder.bind(track, listener);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        private final TextView trackTitle;
        private final TextView trackArtist;
        private final ImageView trackImage;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            trackTitle = itemView.findViewById(R.id.trackTitle);
            trackArtist = itemView.findViewById(R.id.trackArtist);
            trackImage = itemView.findViewById(R.id.trackImage);
        }

        public void bind(final Track track, final OnItemClickListener listener) {
            trackTitle.setText(track.getTitle());
            trackArtist.setText(track.getArtist());
            trackImage.setImageResource(track.getImageResId());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(track);
                }
            });
        }
    }
}
