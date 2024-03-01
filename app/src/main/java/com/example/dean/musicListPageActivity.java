package com.example.dean;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class musicListPageActivity extends Fragment {

    private RecyclerView musicRecylerView;
    private MusicAdapter musicAdapter;
    private static final int PICK_AUDIO_REQUEST = 1;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_music_list_page, container, false);
        setHasOptionsMenu(true);
        CreateRecyclerViewAndAdapter(view);
        CreateToolBar(view);
        return view;
    }

    private void CreateToolBar(View view) {
        Toolbar toolbar = view.findViewById(R.id.musicListPagetoolBar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
    }

    private void CreateRecyclerViewAndAdapter(View view) {
        musicRecylerView = view.findViewById(R.id.musicListRecyclerView);

        musicAdapter = new MusicAdapter(getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        musicRecylerView.setLayoutManager(linearLayoutManager);

        // Truy vấn dữ liệu từ Storage và cập nhật Adapter khi có dữ liệu mới
        getAllAudioFilesFromStorage();

        musicRecylerView.setAdapter(musicAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.musiclistpage_toolbar, menu);
        MenuItem addBoxItem = menu.findItem(R.id.addboxvector);
        addBoxItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Chọn file âm thanh"), PICK_AUDIO_REQUEST);
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri audioUri = data.getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getContext(), audioUri);

            String audioTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String audioArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            // Lấy dữ liệu album art
            byte[] albumArtBytes = retriever.getEmbeddedPicture();
            Bitmap albumArtBitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);

            // Tạo tham chiếu đến Firebase Cloud Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            /*            String audioFileName = "audio_" + System.currentTimeMillis() + ".mp3"; */
            String audioFileName = audioTitle + " - "  +System.currentTimeMillis();
            StorageReference audioRef = storageRef.child("audio/" + audioFileName);
            String musicID = UUID.randomUUID().toString();
            String albumArtFileName = "album_art_" + System.currentTimeMillis() + ".jpg";
            StorageReference albumArtRef = storageRef.child("images/" + albumArtFileName);

            // Lưu Bitmap vào Firebase Storage và xử lý sau khi tải lên thành công
            saveBitmapToFirebaseStorage(albumArtBitmap, albumArtFileName,
                    taskSnapshot -> {
                        // Lấy đường dẫn tải xuống của ảnh từ Firebase Storage
                        albumArtRef.getDownloadUrl().addOnSuccessListener(albumArtDownloadUrl -> {
                            String albumArtUrl = albumArtDownloadUrl.toString();

                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setCustomMetadata("musicID", musicID)
                                    .setCustomMetadata("title", audioTitle)
                                    .setCustomMetadata("artist", audioArtist)
                                    .setCustomMetadata("albumArtUrl", albumArtUrl)
                                    .build();

                            audioRef.putFile(audioUri, metadata)
                                    .addOnSuccessListener(audioUploadTaskSnapshot -> {
                                        audioRef.getDownloadUrl().addOnSuccessListener(audioDownloadUrl -> {
/*                                            String downloadUrl = audioDownloadUrl.toString();
                                            saveFilePathAndMetadataToFirestore(downloadUrl);*/
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

    // Đang kiểm tra lại xem có cần sử dụng không
    private void saveFilePathAndMetadataToFirestore(View view, String downloadUrl) {
        // Lấy metadata từ StorageReference
        storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            String musicID = storageMetadata.getCustomMetadata("musicID");
            String title = storageMetadata.getCustomMetadata("title");
            String artist = storageMetadata.getCustomMetadata("artist");
            String albumArtUrl = storageMetadata.getCustomMetadata("albumArtUrl");

            // Cập nhật metadata của file trong Firebase Storage
            storageRef.updateMetadata(new StorageMetadata.Builder()
                            .setCustomMetadata("audioPath", downloadUrl)
                            .setCustomMetadata("musicID",musicID)
                            .setCustomMetadata("title", title)
                            .setCustomMetadata("musicID", musicID)
                            .setCustomMetadata("artist", artist)
                            .setCustomMetadata("albumArtUrl", albumArtUrl)
                            .build())
                    .addOnSuccessListener(updatedMetadata -> {
                        // Cập nhật thành công, tiếp tục lưu vào Firestore
                        Map<String, Object> fileData = new HashMap<>();
                        fileData.put("audioPath", downloadUrl);
                        fileData.put("title", title);
                        fileData.put("musicID", musicID);
                        fileData.put("artist", artist);
                        fileData.put("albumArtUrl", albumArtUrl);

                        if (firestore == null) {
                            firestore = FirebaseFirestore.getInstance();
                        }

                        firestore.collection("audioCollection")
                                .add(fileData)
                                .addOnSuccessListener(documentReference -> {
                                    // Xử lý thành công
                                    showSnackbar(view, "Cập nhật thành công");
                                })
                                .addOnFailureListener(e -> {
                                    // Xử lý lỗi khi lưu vào Firestore
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi khi cập nhật metadata trong Firebase Storage
                    });
        }).addOnFailureListener(e -> {
            // Xử lý lỗi khi lấy metadata từ Firebase Storage
        });
    }
    private void getAllAudioFilesFromStorage() {
        try {
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
                                String musicID = storageMetadata.getCustomMetadata("musicID");
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
                                        _music.setResourceId(musicID);
                                        _music.setFilePath(item.getName());
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
        catch (Exception e){
            Log.e("error while upload music", e.getMessage());
        }
    }
    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }
}