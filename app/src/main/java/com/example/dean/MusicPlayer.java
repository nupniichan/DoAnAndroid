package com.example.dean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class MusicPlayer extends AppCompatActivity {
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        setData();
        Intent intent = getIntent();
        mediaPlayer = new MediaPlayer();
        String musicUri = intent.getStringExtra("musicFilePath");
        FloatingActionButton playPauseButton = findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    // Nếu đang phát, dừng nhạc
                    stopMusic();
                } else {
                    // Nếu không phát, bắt đầu phát nhạc
                    playMusic(musicUri);
                }
            }
        });

        ImageView returnToMusicList = findViewById(R.id.back_btn);
        returnToMusicList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToMusicListPage();
            }
        });
    }

    private void setData() {
        Intent intent = getIntent();

        ImageView albumArtImageView = findViewById(R.id.cover_art);
        TextView songName = findViewById(R.id.song_name);
        TextView artistName = findViewById(R.id.song_artist);

        String getMusicName = intent.getStringExtra("musicName");
        String getArtistName = intent.getStringExtra("artistName");
        String albumArtFilePath = intent.getStringExtra("albumArtFilePath");

        // Sử dụng Glide để tải hình ảnh từ đường dẫn file
        Glide.with(this)
                .load(Uri.fromFile((new File(albumArtFilePath))))
                .error(R.drawable.ic_launcher_background)
                .into(albumArtImageView);

        songName.setText(getMusicName);
        artistName.setText(getArtistName);
    }

    private void ReturnToMusicListPage(){
        Intent intent = new Intent(this, musicListPageActivity.class);
        startActivity(intent);
    }
    private void playMusic(String filePath) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer(); // Khởi tạo mediaPlayer nếu chưa tồn tại
        }
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true; // Đánh dấu rằng đang phát nhạc
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }
}