package com.example.dean;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dean.Music.MusicPlayer;
import com.example.dean.Music.music;

import java.util.List;

public class RecentlyAddedAdapter extends RecyclerView.Adapter<RecentlyAddedAdapter.RecentlyAddedViewHolder> {
    private Context mContext;
    private List<music> musicList;

    public void SetData(List<music> list) {
        this.musicList = list;
        notifyDataSetChanged();
    }

    public RecentlyAddedAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecentlyAddedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recentlyadded, parent, false);
        return new RecentlyAddedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentlyAddedViewHolder holder, int position) {
        music _music = musicList.get(position);
        if (_music == null)
            return;

        // Load dữ liệu vào ViewHolder
        String albumArtUrl = _music.getAlbumArtBitmap();
        if (albumArtUrl != null) {
            Glide.with(mContext)
                    .load(albumArtUrl)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgMusic);
        } else {
            holder.imgMusic.setImageResource(R.drawable.ic_launcher_background);
        }
        holder.musicTitle.setText(_music.getMusicTitle());
        holder.artistName.setText(_music.getArtist());

        // Thêm sự kiện nghe trực tiếp vào CardView
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic(holder.getAdapterPosition());
            }
        });
    }
    public void playMusic(int position) {
        if (position >= 0 && position < musicList.size()) {
            music selectedMusic = musicList.get(position);
            Intent intent = new Intent(mContext, MusicPlayer.class);
            intent.putExtra("musicName", selectedMusic.getMusicTitle());
            intent.putExtra("artistName", selectedMusic.getArtist());
            intent.putExtra("albumArtFilePath", selectedMusic.getAlbumArtBitmap());
            intent.putExtra("musicFilePath", selectedMusic.getUriFilePath());
            mContext.startActivity(intent);
        }
    }
    @Override
    public int getItemCount() {
        if (musicList != null) {
            return musicList.size();
        }
        return 0;
    }

    public class RecentlyAddedViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private ImageView imgMusic;
        private TextView musicTitle;
        private TextView artistName;

        public RecentlyAddedViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.recentlyAddedCardView);
            imgMusic = itemView.findViewById(R.id.MadeForYouImageView);
            musicTitle = itemView.findViewById(R.id.MadeForYouTitle);
            artistName = itemView.findViewById(R.id.MadeForYouAuthor);
        }
    }
}
