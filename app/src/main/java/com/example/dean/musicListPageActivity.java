package com.example.dean;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.ColorFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dean.Music.Adapter.UserMusicAdapter;
import com.example.dean.Music.music;
import com.example.dean.Utils.Utils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class musicListPageActivity extends Fragment {

    private RecyclerView musicRecylerView;
    private UserMusicAdapter musicAdapter;
    private ProgressBar loadingProgressBar;
    private TextView loadingText;
    private List<music> allMusicList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_music_list_page, container, false);
        setHasOptionsMenu(true);

        loadingProgressBar = view.findViewById(R.id.adminLoadingProgressBar);
        loadingText = view.findViewById(R.id.loadingText);
        CreateRecyclerViewAndAdapter(view);
        CreateToolBar(view);
        return view;
    }


    private void CreateToolBar(View view) {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        Toolbar toolbar = view.findViewById(R.id.musicListPagetoolBar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setBackground(getResources().getDrawable(currentMode == AppCompatDelegate.MODE_NIGHT_YES ?
                R.drawable.toolbarcolor_light : R.drawable.toolbarcolor_dark));
        toolbar.setTitleTextColor(getResources().getColor(currentMode == AppCompatDelegate.MODE_NIGHT_YES ?
                R.color.switch_text_color_dark : R.color.switch_text_color_light));
    }

    private void CreateRecyclerViewAndAdapter(View view) {
        musicRecylerView = view.findViewById(R.id.musicListRecyclerView);

        musicAdapter = new UserMusicAdapter(getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        musicRecylerView.setLayoutManager(linearLayoutManager);

        getAllAudioFilesFromStorage();

        musicRecylerView.setAdapter(musicAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.musicuserlistpage, menu);
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int color = getResources().getColor(currentMode == AppCompatDelegate.MODE_NIGHT_YES ? R.color.toolbarcolor_light : R.color.toolbarcolor_dark);
        ColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
        MenuItem searchItem = menu.findItem(R.id.searchItem);
        SearchView searchView = (SearchView) searchItem.getActionView();
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        searchIcon.setColorFilter(colorFilter);
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(color);
        searchEditText.setHintTextColor(color);
        searchView.setQueryHint("Nhập tên bài nhạc");

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
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchView.setIconified(true);
            }
        });
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

                                        Log.d("StorageData", "Size: " + musicList.size());

                                        musicAdapter.SetData(musicList);
                                        loadingProgressBar.setVisibility(View.GONE);
                                        loadingText.setVisibility(View.GONE);
                                        mediaPlayer.release();
                                    } catch (IOException | IllegalArgumentException | SecurityException e) {
                                        e.printStackTrace();
                                        Log.e("StorageData", "Error preparing MediaPlayer: " + e.getMessage());
                                        loadingProgressBar.setVisibility(View.GONE);
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e("StorageData", "Error getting download URL: " + e.getMessage());
                                    loadingProgressBar.setVisibility(View.GONE);
                                });
                            }).addOnFailureListener(e -> {
                                Log.e("StorageData", "Error getting metadata: " + e.getMessage());
                                loadingProgressBar.setVisibility(View.GONE);
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("StorageData", "Error: " + e.getMessage());
                        loadingProgressBar.setVisibility(View.GONE);
                    });
        } catch (Exception e) {
            Log.e("error while upload music", e.getMessage());
            loadingProgressBar.setVisibility(View.GONE);
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
        musicAdapter.SetData(filteredMusicList);
    }
}