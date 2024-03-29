package com.example.dean.Music.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dean.Music.MusicPlayer;
import com.example.dean.Music.music;
import com.example.dean.R;
import com.example.dean.BottomSheet.UserMusicOptionsBottomSheet;

import java.util.List;

public class UserMusicAdapter extends RecyclerView.Adapter<UserMusicAdapter.UserMusicViewHolder> {

    private Context mContext;
    private List<music> musicList;

    public UserMusicAdapter(Context mContext) {
        this.mContext = mContext;
    }
    public void SetData(List<music> list){
        this.musicList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserMusicAdapter.UserMusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item,parent,false);
        return new UserMusicAdapter.UserMusicViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull UserMusicViewHolder holder, int position) {
        music _music = musicList.get(position);
        ImageButton musicOptionMenu = holder.itemView.findViewById(R.id.musicOptionMenu);
        musicOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserMusicOptionsBottomSheet bottomDialog = new UserMusicOptionsBottomSheet();
                bottomDialog.setPlayClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        playMusic(position);
                        bottomDialog.dismiss();
                    }
                });
                bottomDialog.show(((AppCompatActivity)mContext).getSupportFragmentManager(),"bottom_dialog");
            }
        });
        if (_music == null)
            return;

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
        holder.musicTitle.setSelected(true);
        holder.artistName.setSelected(true);
        holder.musicTitle.setMaxLines(1);
        holder.artistName.setMaxLines(1);
        int musicLengthInMillis = (int)_music.getMusicLength();
        int seconds = (int) (musicLengthInMillis / 1000) % 60;
        int minutes = (int) ((musicLengthInMillis / (1000 * 60)) % 60);
        holder.musicLength.setText(String.format("%02d:%02d", minutes, seconds));
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
    public List<music> getData() {
        return musicList;
    }
    @Override
    public int getItemCount() {
        if (musicList != null){
            return musicList.size();
        }
        return 0;
    }

    public class UserMusicViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgMusic;
        private TextView musicTitle;
        private TextView artistName;
        private TextView musicLength;
        public UserMusicViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMusic = itemView.findViewById(R.id.musicImageView);
            musicTitle = itemView.findViewById(R.id.musicNameTextView);
            artistName = itemView.findViewById(R.id.ArtistTextView);
            musicLength = itemView.findViewById(R.id.musicLengthTextView);
        }
    }
}