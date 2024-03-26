package com.example.dean;

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

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private Context mContext;
    private List<music> musicList;
    public MusicAdapter(Context mContext) {
        this.mContext = mContext;
        this.musicList = new ArrayList<>();
    }

    public void setData(List<music> list) {
        this.musicList.clear();
        this.musicList.addAll(list);
        notifyDataSetChanged();
    }

    public List<music> getData() {
        return musicList;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
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
                bottomDialog.setOnEditClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = holder.getAdapterPosition();
                        editMusic(mContext, position);
                        bottomDialog.dismiss();
                    }
                });

                bottomDialog.setOnDeleteClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = holder.getAdapterPosition();
                        removeMusic(position);
                        bottomDialog.dismiss();
                    }
                });

                bottomDialog.setPlayClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        playMusic(position);
                        bottomDialog.dismiss();
                    }
                });
                bottomDialog.setOnInformationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = holder.getAdapterPosition();
                        // musicInformation(position);
                        bottomDialog.dismiss();
                    }
                });
                bottomDialog.show(((AppCompatActivity)mContext).getSupportFragmentManager(),"bottom_dialog");
            }
        });

        // Tự động phát nhạc khi người dùng chọn một bài hát trong danh sách
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                playMusic(position);
            }
        });

        // Các phương thức xử lý sự kiện khác

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

    @Override
    public int getItemCount() {
        return musicList != null ? musicList.size() : 0;
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgMusic;
        private TextView musicTitle;
        private TextView artistName;
        private TextView musicLength;
        private ImageButton btnPrevious;
        private ImageButton btnSkip;
        private ImageButton btnReplay;
        private ImageButton btnShuffle;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMusic = itemView.findViewById(R.id.musicImageView);
            musicTitle = itemView.findViewById(R.id.musicNameTextView);
            artistName = itemView.findViewById(R.id.ArtistTextView);
            musicLength = itemView.findViewById(R.id.musicLengthTextView);
            btnPrevious = itemView.findViewById(R.id.id_prev);
            btnSkip = itemView.findViewById(R.id.id_skip);
            btnReplay = itemView.findViewById(R.id.id_repeat);
            btnShuffle = itemView.findViewById(R.id.id_shuffle);
        }
    }

    private void removeMusic(int position) {
        if (position >= 0 && position < musicList.size()) {
            music musicToRemove = musicList.get(position);
            RemoveFragment removeMusicFragment = new RemoveFragment(musicToRemove);
            if (mContext instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) mContext;
                removeMusicFragment.show(activity.getSupportFragmentManager(), "remove_music");
            }
        }
    }
    public void editMusic(Context context, int position) {
        if (position >= 0 && position < musicList.size()) {
            music musicToEdit = musicList.get(position);

            EditFragment editFragment = EditFragment.newInstance(musicToEdit);
            editFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "edit_fragment");
        }
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
/*    public void musicInformation(int position) {
        if (position >= 0 && position < musicList.size()) {
            music selectedMusic = musicList.get(position);
            RemoveFragment removeMusicFragment = new RemoveFragment(selectedMusic);
            if (mContext instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) mContext;
                removeMusicFragment.show(activity.getSupportFragmentManager(), "remove_music");
            }
        }
    }*/
}
