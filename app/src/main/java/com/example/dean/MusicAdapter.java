package com.example.dean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private Context mContext;
    private List<music> musicList;
    private static final String MUSIC_PREFERENCE = "music_preference";
    private static final String MUSIC_LIST_KEY = "music_list";
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
        ImageButton musicOptionMenu = holder.itemView.findViewById(R.id.musicOptionMenu);
        musicOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicOptionsBottomSheet bottomDialog = new MusicOptionsBottomSheet();
                bottomDialog.setOnDeleteClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = holder.getAdapterPosition();
                        // Thực hiện hành động xoá dựa trên vị trí hoặc thông tin từ adapter
                        removeMusic(position);
                        bottomDialog.dismiss();
                    }
                });
                bottomDialog.show(((AppCompatActivity)mContext).getSupportFragmentManager(),"bottom_dialog");
            }
        });
        if (_music == null)
            return;
        holder.imgMusic.setImageResource(_music.getResourceId());
        holder.musicTitle.setText(_music.getMusicTitle());
        holder.artistName.setText(_music.getArtist());

        int musicLengthInMillis = (int)_music.getMusicLength();
        int seconds = (int) (musicLengthInMillis / 1000) % 60;
        int minutes = (int) ((musicLengthInMillis / (1000 * 60)) % 60);
        holder.musicLength.setText(String.format("%02d:%02d", minutes, seconds));

    }
    public void removeMusic(int position) {
        if (position >= 0 && position < musicList.size()) {
            musicList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, musicList.size());

            // Cập nhật danh sách nhạc đã xoá vào SharedPreferences
            Gson gson = new Gson();
            String updatedJson = gson.toJson(musicList);
            mContext.getSharedPreferences(MUSIC_PREFERENCE, Context.MODE_PRIVATE).edit().putString(MUSIC_LIST_KEY, updatedJson).apply();

        }
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