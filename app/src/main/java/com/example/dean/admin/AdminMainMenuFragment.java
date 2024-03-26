package com.example.dean.admin;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.dean.Music.Adapter.MusicAdapter;
import com.example.dean.R;
import com.example.dean.Utils.Utils;
import com.example.dean.Music.music;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AdminMainMenuFragment extends Fragment {

    private RecyclerView musicRecylerView;
    private MusicAdapter musicAdapter;
    private static final int PICK_AUDIO_REQUEST = 1;
    private ProgressBar loadingProgressBar;
    private TextView loadingText;
    private Context mContext;

    private List<music> allMusicList = new ArrayList<>();
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_main_menu, container, false);
        setHasOptionsMenu(true);
        loadingProgressBar = view.findViewById(R.id.adminLoadingProgressBar);
        loadingText = view.findViewById(R.id.adminloadingText);
        SearchView searchView = view.findViewById(R.id.adminsearchView);
        CreateRecyclerViewAndAdapter(view);
        CreateToolBar(view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDataFromFirebase(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    getAllAudioFilesFromStorage();
                }
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
        // Xử lý sự kiện khi người dùng chạm vào vùng ngoài của SearchView
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Kiểm tra nếu sự kiện chạm là ACTION_DOWN (người dùng bắt đầu chạm vào màn hình)
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Kiểm tra nếu SearchView đang được focus
                    if (searchView.hasFocus()) {
                        // Xóa focus từ SearchView, điều này sẽ đóng bàn phím
                        searchView.clearFocus();
                    }
                }
                return false;
            }
        });
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

        getAllAudioFilesFromStorage();

        musicRecylerView.setAdapter(musicAdapter);
    }


    private void getAllAudioFilesFromStorage() {
        try {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("audio");

            storageRef.listAll()
                    .addOnSuccessListener(listResult -> {
                        allMusicList.clear();

                        List<music> musicList = new ArrayList<>();

                        for (StorageReference item : listResult.getItems()) {
                            music _music = new music();
                            _music.setMusicTitle(item.getName());
                            _music.setFilePath(item.getPath());

                            item.getMetadata().addOnSuccessListener(storageMetadata -> {
                                String audioTitle = storageMetadata.getCustomMetadata("title");
                                String audioArtist = storageMetadata.getCustomMetadata("artist");
                                String albumArt = storageMetadata.getCustomMetadata("albumArtUrl");
                                String musicID = storageMetadata.getCustomMetadata("musicID");
                                String albumArtFileName = storageMetadata.getCustomMetadata("albumArtName");

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
                                        _music.setAlbumArtName(albumArtFileName);
                                        _music.setUriFilePath(downloadUrl.toString());
                                        _music.setResourceId(musicID);
                                        _music.setFilePath(item.getName());
                                        musicList.add(_music);

                                        Log.d("StorageData", "Size: " + musicList.size());

                                        musicAdapter.setData(musicList);
                                        loadingProgressBar.setVisibility(View.GONE);
                                        loadingText.setVisibility(View.GONE);
                                        mediaPlayer.release();
                                    } catch (IOException | IllegalArgumentException | SecurityException e) {
                                        e.printStackTrace();
                                        Log.e("StorageData", "Error preparing MediaPlayer: " + e.getMessage());
                                        loadingProgressBar.setVisibility(View.GONE);
                                        loadingText.setVisibility(View.GONE);
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e("StorageData", "Error getting download URL: " + e.getMessage());
                                    loadingProgressBar.setVisibility(View.GONE);
                                    loadingText.setVisibility(View.GONE);
                                });
                            }).addOnFailureListener(e -> {
                                Log.e("StorageData", "Error getting metadata: " + e.getMessage());
                                loadingProgressBar.setVisibility(View.GONE);
                                loadingText.setVisibility(View.GONE);
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("StorageData", "Error: " + e.getMessage());
                        loadingProgressBar.setVisibility(View.GONE);
                        loadingText.setVisibility(View.GONE);
                    });
        } catch (Exception e) {
            Log.e("error while upload music", e.getMessage());
            loadingProgressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        }
    }

    public void filterDataFromFirebase(String searchText) {
        List<music> filteredMusicList = new ArrayList<>();
        if (searchText.length() < 1) {
            getAllAudioFilesFromStorage();
            filteredMusicList.addAll(musicAdapter.getData());
        } else {
            for (music musicItem : musicAdapter.getData()) {
                if (Utils.containsAllChars(musicItem.getMusicTitle().toLowerCase(),searchText.toLowerCase())) {
                    filteredMusicList.add(musicItem);
                }
            }
        }
        musicAdapter.setData(filteredMusicList);
    }
}
