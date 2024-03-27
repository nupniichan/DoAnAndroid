package com.example.dean.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dean.MainActivity;
import com.example.dean.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class AdminActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    Context context = this;
    private static final int PICK_AUDIO_REQUEST = 1;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Toolbar toolbar = findViewById(R.id.adminMusicListPagetoolBar);
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        toolbar.setBackground(getResources().getDrawable(currentMode == AppCompatDelegate.MODE_NIGHT_YES ?
                R.drawable.toolbarcolor_light : R.drawable.toolbarcolor_dark));
        toolbar.setTitleTextColor(getResources().getColor(currentMode == AppCompatDelegate.MODE_NIGHT_YES ?
                R.color.switch_text_color_dark : R.color.switch_text_color_light));
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        AdminMainMenuFragment adminMainMenuFragment = new AdminMainMenuFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.adminFragment, adminMainMenuFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.addMusicDrawerMenu:
                    {
                        addMusic();
                        break;
                    }
                    case R.id.reloadAdminPageDrawerMenu:
                    {
                        reloadFragment();
                        break;
                    }
                    case R.id.backtoUserActivityDrawerMenu:
                    {
                        // Tạo Intent để chuyển đến MainActivity
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        break;
                    }
                }
                return false;
            }
        });
        navigationView.bringToFront();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addMusic() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn file âm thanh"), PICK_AUDIO_REQUEST);
    }
    private void reloadFragment() {
        AdminMainMenuFragment adminMainMenuFragment = new AdminMainMenuFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.adminFragment, adminMainMenuFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri audioUri = data.getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, audioUri);

            String audioTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String audioArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            // Lấy dữ liệu album art
            byte[] albumArtBytes = retriever.getEmbeddedPicture();
            Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String audioFileName = audioTitle + " - "  +System.currentTimeMillis();
            StorageReference audioRef = storageRef.child("audio/" + audioFileName);
            String musicID = UUID.randomUUID().toString();
            String albumArtFileName = "album_art_" + System.currentTimeMillis() + ".jpg";
            StorageReference albumArtRef = storageRef.child("images/" + albumArtFileName);

            // Lưu Bitmap vào Firebase Storage và xử lý sau khi tải lên thành công
            saveBitmapToFirebaseStorage(albumArtBitmap, albumArtFileName,
                    taskSnapshot -> {
                        albumArtRef.getDownloadUrl().addOnSuccessListener(albumArtDownloadUrl -> {
                            String albumArtUrl = albumArtDownloadUrl.toString();

                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setCustomMetadata("musicID", musicID)
                                    .setCustomMetadata("title", audioTitle)
                                    .setCustomMetadata("artist", audioArtist)
                                    .setCustomMetadata("albumArtUrl", albumArtUrl)
                                    .setCustomMetadata("albumArtName", albumArtFileName)
                                    .build();

                            audioRef.putFile(audioUri, metadata)
                                    .addOnSuccessListener(audioUploadTaskSnapshot -> {
                                        audioRef.getDownloadUrl().addOnSuccessListener(audioDownloadUrl -> {
                                            Toast.makeText(context, "Thêm nhạc thành công!", Toast.LENGTH_SHORT).show();
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Thêm nhạc vào firebase thất bại!", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    },
                    e -> {
                    });
        }
    }
    private void saveBitmapToFirebaseStorage(Bitmap bitmap, String fileName,
                                             OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener,
                                             OnFailureListener onFailureListener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images/" + fileName);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}
