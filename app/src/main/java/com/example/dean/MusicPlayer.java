package com.example.dean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        Bitmap receivedBitmap = SharedBitmapHolder.getInstance().getSharedBitmap();
        songName.setText(getMusicName);
        artistName.setText(getArtistName);
        albumArtImageView.setImageBitmap(receivedBitmap);
    }
}