package com.example.dean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.reflect.TypeToken;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class musicListPageActivity extends AppCompatActivity {

    private RecyclerView musicRecylerView;
    private MusicAdapter musicAdapter;
    private static final String MUSIC_PREFERENCE = "music_preference";
    private static final String MUSIC_LIST_KEY = "music_list";
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_page);

        // Khởi tạo RecyclerView và Adapter
        CreateRecyclerViewAndAdapter();

        // Khởi tạo toolBar của trang danh sách nhạc
        CreateToolBar();

        // Button quay về lại trang chủ
        CreateHomeButton();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    private void CreateHomeButton() {
        Button homeButton = findViewById(R.id.button1);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToHomePage();
            }
        });
    }

    private void CreateToolBar() {
        Toolbar toolbar = findViewById(R.id.musicPageListToolBar);
        setSupportActionBar(toolbar);
    }

    private void CreateRecyclerViewAndAdapter() {
        musicRecylerView = findViewById(R.id.musicListRecyclerView);
        musicAdapter = new MusicAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        musicRecylerView.setLayoutManager(linearLayoutManager);

        // Truy vấn dữ liệu từ Storage và cập nhật Adapter khi có dữ liệu mới
        getAllAudioFilesFromStorage();

        musicRecylerView.setAdapter(musicAdapter);
    }

    private void getAllAudioFilesFromStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("audio");

        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<music> musicList = new ArrayList<>();

                    for (StorageReference item : listResult.getItems()) {
                        // Tạo một đối tượng music từ thông tin của StorageReference
                        music _music = new music();
                        _music.setMusicTitle(item.getName());
                        _music.setFilePath(item.getPath());

                        // Lấy thông tin từ metadata của file
                        item.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String audioTitle = storageMetadata.getCustomMetadata("title");
                            String audioArtist = storageMetadata.getCustomMetadata("artist");
                            String albumArt = storageMetadata.getCustomMetadata("albumArtUrl");

                            item.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(downloadUrl.toString());
                                    mediaPlayer.prepare();
                                    int durationInMillis = mediaPlayer.getDuration();
                                    _music.setMusicTitle(audioTitle != null ? audioTitle : item.getName());
                                    _music.setArtist(audioArtist != null ? audioArtist : "Unknown Artist");
                                    _music.setMusicLength(durationInMillis);
                                    _music.setAlbumArtBitmap(albumArt);
                                    _music.setUriFilePath(downloadUrl.toString());

                                    musicList.add(_music);

                                    // Log để kiểm tra dữ liệu trả về từ Storage
                                    Log.d("StorageData", "Size: " + musicList.size());

                                    musicAdapter.SetData(musicList);

                                    // Giải phóng MediaPlayer khi đã sử dụng xong
                                    mediaPlayer.release();
                                } catch (IOException | IllegalArgumentException | SecurityException e) {
                                    e.printStackTrace();
                                    Log.e("StorageData", "Error preparing MediaPlayer: " + e.getMessage());
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("StorageData", "Error getting download URL: " + e.getMessage());
                            });
                        }).addOnFailureListener(e -> {
                            Log.e("StorageData", "Error getting metadata: " + e.getMessage());
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("StorageData", "Error: " + e.getMessage());
                });
    }



    private List<music> getMusicList() {
        List<music> list = new ArrayList<>();
        return list;
    }
    private static final int PICK_AUDIO_REQUEST = 1;

    // Tạo option menu (nghĩa là cái bấm vào rồi nó hiện cái bảng ở dưới ấy)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.musiclistpage_toolbar,menu);
        MenuItem addBoxItem = menu.findItem(R.id.addboxvector);
        addBoxItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                // Để người dùng bấm cái phím + để add file vô
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Chọn file âm thanh"), PICK_AUDIO_REQUEST);
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri audioUri = data.getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, audioUri);

            String audioTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String audioArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            // Lấy dữ liệu album art
            byte[] albumArtBytes = retriever.getEmbeddedPicture();
            Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);

            // Tạo tham chiếu đến Firebase Cloud Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String audioFileName = "audio_" + System.currentTimeMillis() + ".mp3"; // Tên tệp tin trên Firebase Cloud Storage
            StorageReference audioRef = storageRef.child("audio/" + audioFileName);
            String albumArtFileName = "album_art_" + System.currentTimeMillis() + ".jpg";
            StorageReference albumArtRef = storageRef.child("images/" + albumArtFileName);

            // Lưu Bitmap vào Firebase Storage và xử lý sau khi tải lên thành công
            saveBitmapToFirebaseStorage(albumArtBitmap, albumArtFileName,
                    taskSnapshot -> {
                        // Lấy đường dẫn tải xuống của ảnh từ Firebase Storage
                        albumArtRef.getDownloadUrl().addOnSuccessListener(albumArtDownloadUrl -> {
                            String albumArtUrl = albumArtDownloadUrl.toString();

                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setCustomMetadata("title", audioTitle)
                                    .setCustomMetadata("artist", audioArtist)
                                    .setCustomMetadata("albumArtUrl", albumArtUrl)
                                    .build();

                            audioRef.putFile(audioUri, metadata)
                                    .addOnSuccessListener(audioUploadTaskSnapshot -> {
                                        audioRef.getDownloadUrl().addOnSuccessListener(audioDownloadUrl -> {
                                            String downloadUrl = audioDownloadUrl.toString();
                                            saveFilePathAndMetadataToFirestore(downloadUrl);
                                        });
                                    })
                                    .addOnFailureListener(e -> {
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

    private void saveFilePathAndMetadataToFirestore(String downloadUrl) {
        // Lấy metadata từ StorageReference
        storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            String title = storageMetadata.getCustomMetadata("title");
            String artist = storageMetadata.getCustomMetadata("artist");
            String albumArtUrl = storageMetadata.getCustomMetadata("albumArtUrl");

            // Tạo một tài liệu mới với các thông tin
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("audioPath", downloadUrl);
            fileData.put("title", title);
            fileData.put("artist", artist);
            fileData.put("albumArtUrl", albumArtUrl);

            // Đảm bảo firestore đã được khởi tạo
            if (firestore == null) {
                firestore = FirebaseFirestore.getInstance();
            }

            // Lưu tài liệu vào Firestore
            firestore.collection("audioCollection")
                    .add(fileData)
                    .addOnSuccessListener(documentReference -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }).addOnFailureListener(e -> {
        });
    }

    public void ReturnToHomePage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}