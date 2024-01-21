package com.example.dean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

public class MusicPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
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

        ImageView returnToMusicList = findViewById(R.id.back_btn);
        returnToMusicList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToMusicListPage();
            }
        });
    }
    private void ReturnToMusicListPage(){
        Intent intent = new Intent(this, musicListPageActivity.class);
        startActivity(intent);
    }
}