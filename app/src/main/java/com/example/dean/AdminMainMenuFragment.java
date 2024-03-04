package com.example.dean;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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


public class AdminMainMenuFragment extends Fragment {

    Button backToUserMenu;
    private RecyclerView musicRecylerView;
    private MusicAdapter musicAdapter;
    private static final int PICK_AUDIO_REQUEST = 1;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_main_menu, container, false);
        setHasOptionsMenu(true);
        backToUserMenu = view.findViewById(R.id.btnBackToUserMenu);
        backToUserMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuActivity mainMenuFragment = new MainMenuActivity();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, mainMenuFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        Button btnAdminReload = view.findViewById(R.id.btnAdminReload);
        btnAdminReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi phương thức để reload fragment
                reloadFragment();
            }
        });
        CreateRecyclerViewAndAdapter(view);
        CreateToolBar(view);
        return view;
    }
    private void reloadFragment() {
        AdminMainMenuFragment adminMainMenuFragment = new AdminMainMenuFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, adminMainMenuFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
        MenuItem searchItem = menu.findItem(R.id.searchItem);
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

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Nhập tên bài nhạc");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDataFromFirebase(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getAllAudioFilesFromStorage();
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchView.setIconified(true);
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

    private void filterDataFromFirebase(String searchText) {
        // Tạo danh sách mới để chứa dữ liệu lọc
        List<music> filteredMusicList = new ArrayList<>();

        // Lọc dữ liệu dựa trên searchText
        for (music musicItem : musicAdapter.getData()) {
            if (musicItem.getMusicTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filteredMusicList.add(musicItem);
            }
        }

        // Cập nhật RecyclerView với danh sách dữ liệu lọc
        musicAdapter.SetData(filteredMusicList);
    }
}