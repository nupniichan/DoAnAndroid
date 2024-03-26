package com.example.dean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import android.view.View;
import androidx.core.content.ContextCompat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayer extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    SeekBar timeLineBar;
    TextView currentTime;
    TextView duration;
    private ArrayList<String> songPaths;
    private int currentPosition=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        mediaPlayer = new MediaPlayer();
        SetData();

        Intent intent = getIntent();
        String musicUri = intent.getStringExtra("musicFilePath");
        try {
            PrepareData(musicUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Phan next, previos
        songPaths = intent.getStringArrayListExtra("songPaths");
        currentPosition = intent.getIntExtra("currentPosition", 0);
        FloatingActionButton nextButton = findViewById(R.id.id_skip);
        FloatingActionButton prevButton = findViewById(R.id.id_prev);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition++;
                if (currentPosition >= songPaths.size()) currentPosition = 0;
                try {
                    PrepareData(songPaths.get(currentPosition));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPosition--;
                if (currentPosition < 0) currentPosition = songPaths.size() - 1;
                try {
                    PrepareData(songPaths.get(currentPosition));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        PlayButton(musicUri);
        CreateTimeLineBar();
        CompletePlaying();
        ReturnToMusicListPageButton();
    }

    private void CompletePlaying() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                FloatingActionButton playPauseButton = findViewById(R.id.play_pause);
                playPauseButton.setImageResource(R.drawable.baseline_play_arrow);
                timeLineBar.setProgress(0);
                currentTime.setText(formatDuration(0));
            }
        });
    }

    private void CreateTimeLineBar() {
        timeLineBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(formatDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    private void ReturnToMusicListPageButton() {
        ImageView returnToMusicList = findViewById(R.id.back_btn);
        returnToMusicList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToMusicListPage();
                mediaPlayer.stop();
            }
        });
    }

    private void PlayButton(String musicUri) {
        FloatingActionButton playPauseButton = findViewById(R.id.play_pause);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        PauseMusic(playPauseButton);
                    } else {
                        if (mediaPlayer.getCurrentPosition() > 0) {
                            ResumeMusic();
                        } else {
                            StartPLaying(musicUri);
                        }
                        playPauseButton.setImageResource(R.drawable.baseline_pause);
                    }
                } catch (IOException | IllegalStateException e) {
                    Log.e("MusicPlayer", "Error", e);
                }
            }
        });
    }

    private void StartPLaying(String musicUri) throws IOException {
/*        mediaPlayer.setDataSource(musicUri);
        mediaPlayer.prepare();
        mediaPlayer.seekTo(0);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int totalDuration = mediaPlayer.getDuration();
                timeLineBar.setMax(totalDuration);

                currentTime.setText(formatDuration(0));
                duration.setText(formatDuration(totalDuration));

                updateSeekBar();
            }
        });*/

        ResumeMusic();
    }

    private void PrepareData(String musicUri) throws IOException{
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference musicRef = storageRef.child("music/" + musicUri);
        try {
            final File localFile = File.createTempFile("tempMusic", "mp3");

            musicRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // File tải xuống thành công, bắt đầu phát nhạc từ localFile
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(localFile.getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        // Cập nhật giao diện và các thông tin khác
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Xử lý khi tải xuống thất bại
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setDataSource(musicUri);
        mediaPlayer.prepare();
        mediaPlayer.seekTo(0);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int totalDuration = mediaPlayer.getDuration();
                timeLineBar.setMax(totalDuration);

                currentTime.setText(formatDuration(0));
                duration.setText(formatDuration(totalDuration));

                updateSeekBar();
            }
        });
    }

    private void PauseMusic(FloatingActionButton playPauseButton) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.baseline_play_arrow);
        }
    }

    private void ResumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateSeekBar();
        }
    }

    private void SetData() {
        Intent intent = getIntent();

        ImageView albumArtImageView = findViewById(R.id.cover_art);
        TextView songName = findViewById(R.id.song_name);
        TextView artistName = findViewById(R.id.song_artist);
        currentTime = findViewById(R.id.startingTime);
        duration = findViewById(R.id.endingTime);
        timeLineBar = findViewById(R.id.timeLineBar);

        currentTime.setText(formatDuration(0));
        String getMusicName = intent.getStringExtra("musicName");
        String getArtistName = intent.getStringExtra("artistName");
        String albumArtFilePath = intent.getStringExtra("albumArtFilePath");

        // Sử dụng Glide để tải hình ảnh từ đường dẫn file
        Glide.with(this)
                .asBitmap()
                .load(albumArtFilePath)
                .error(R.drawable.ic_launcher_background)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap originalBitmap, Transition<? super Bitmap> transition) {
                        Bitmap paletteBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                        int dominantColor = Palette.from(paletteBitmap).generate().getDominantColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                        View musicPlayerView = findViewById(R.id.music_player_view);
                        musicPlayerView.setBackgroundColor(dominantColor);
                        ImageView albumArtImageView = findViewById(R.id.cover_art);
                        albumArtImageView.setImageBitmap(originalBitmap);
                    }
                });
        songName.setText(getMusicName);
        artistName.setText(getArtistName);
    }


    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            timeLineBar.setProgress(currentPosition);
            currentTime.setText(formatDuration(currentPosition));
            timeLineBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            }, 1000);
        }
    }
    private String formatDuration(int durationInMillis) {
        int seconds = (durationInMillis / 1000) % 60;
        int minutes = (durationInMillis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void ReturnToMusicListPage(){
        // Kiểm tra xem có Fragment nào trong BackStack hay không
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            // Nếu có Fragment trong BackStack, quay lại Fragment trước đó
            getSupportFragmentManager().popBackStack();
        } else {
            // Nếu không có Fragment trong BackStack, thoát ứng dụng hoặc thực hiện hành động khác
            super.onBackPressed();
        }
    }
}