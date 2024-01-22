package com.example.dean;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
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
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
                bottomDialog.setPlayClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        playMusic(position);
                        bottomDialog.dismiss();
                    }
                });
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

        Bitmap albumArtBitmap = _music.getAlbumArtBitmap();
        if (albumArtBitmap != null) {
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(_music.getFilePath())))
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imgMusic);
        } else {
            // Nếu không có, đặt ảnh mặc định hoặc thực hiện xử lý khác
            holder.imgMusic.setImageResource(R.drawable.ic_launcher_background);
        }

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
    public void playMusic(int position) {
        if (position >= 0 && position < musicList.size()) {
            music selectedMusic = musicList.get(position);
            Intent intent = new Intent(mContext, MusicPlayer.class);
            intent.putExtra("musicName", selectedMusic.getMusicTitle());
            intent.putExtra("artistName", selectedMusic.getArtist());
            intent.putExtra("albumArtFilePath", selectedMusic.getFilePath());
            intent.putExtra("musicFilePath", selectedMusic.getUriFilePath());
            mContext.startActivity(intent);
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