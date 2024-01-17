package com.example.dean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private Context mContext;
    private List<music> musicList;

    public MusicAdapter(Context mContext) {
        this.mContext = mContext;
    }
    public void SetData(List<music> list){
        this.musicList = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item,parent,false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        music _music = musicList.get(position);
        if (_music == null)
            return;
        holder.imgMusic.setImageResource(_music.getResourceId());
        holder.musicTitle.setText(_music.getMusicTitle());
        holder.artistName.setText(_music.getArtist());
        holder.musicLength.setText(_music.getMusicLength());
    }

    @Override
    public int getItemCount() {
        if (musicList != null){
            return musicList.size();
        }
        return 0;
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgMusic;
        private TextView musicTitle;
        private TextView artistName;
        private TextView musicLength;
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMusic = itemView.findViewById(R.id.musicImageView);
            musicTitle = itemView.findViewById(R.id.musicNameTextView);
            artistName = itemView.findViewById(R.id.ArtistTextView);
            musicLength = itemView.findViewById(R.id.musicLengthTextView);
        }
    }
}
