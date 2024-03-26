package com.example.dean;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dean.Account.AccountActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainMenuActivity extends Fragment {
    private RecyclerView musicRecylerView;
    private RecentlyAddedAdapter musicAdapter;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    private void CreateRecyclerViewAndAdapter(View view) {
        musicRecylerView = view.findViewById(R.id.recentlyAddedRecyclerView);

        musicAdapter = new RecentlyAddedAdapter(getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        musicRecylerView.setLayoutManager(linearLayoutManager);

        getRecentAudioFilesFromStorage();

        musicRecylerView.setAdapter(musicAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main_menu, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        setHasOptionsMenu(true);
        CreateRecyclerViewAndAdapter(view);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
        if (id == R.id.person_vector) {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new AccountActivity())
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void getRecentAudioFilesFromStorage() {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("audio");

            storageRef.listAll()
                    .addOnSuccessListener(listResult -> {
                        Map<StorageReference, Long> lastModifiedTimeMap = new HashMap<>();

                        for (StorageReference item : listResult.getItems()) {
                            item.getMetadata().addOnSuccessListener(storageMetadata -> {
                                long lastModifiedTime = storageMetadata.getUpdatedTimeMillis(); // Thay đổi từ getCreationTimeMillis()
                                lastModifiedTimeMap.put(item, lastModifiedTime);

                                if (lastModifiedTimeMap.size() == listResult.getItems().size()) {
                                    List<StorageReference> sortedItems = listResult.getItems()
                                            .stream()
                                            .sorted(Comparator.comparingLong(lastModifiedTimeMap::get).reversed())
                                            .limit(3)  // Chỉ lấy 3 tệp mới nhất
                                            .collect(Collectors.toList());

                                    List<music> musicList = new ArrayList<>();

                                    for (StorageReference sortedItem : sortedItems) {
                                        music _music = new music();
                                        _music.setMusicTitle(sortedItem.getName());
                                        _music.setFilePath(sortedItem.getPath());

                                        sortedItem.getMetadata().addOnSuccessListener(sortedItemMetadata -> {
                                            String audioTitle = sortedItemMetadata.getCustomMetadata("title");
                                            String audioArtist = sortedItemMetadata.getCustomMetadata("artist");
                                            String albumArt = sortedItemMetadata.getCustomMetadata("albumArtUrl");
                                            String musicID = sortedItemMetadata.getCustomMetadata("musicID");

                                            sortedItem.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                                                MediaPlayer mediaPlayer = new MediaPlayer();
                                                try {
                                                    mediaPlayer.setDataSource(downloadUrl.toString());
                                                    mediaPlayer.prepare();
                                                    int durationInMillis = mediaPlayer.getDuration();
                                                    _music.setMusicTitle(audioTitle != null ? audioTitle : sortedItem.getName());
                                                    _music.setArtist(audioArtist != null ? audioArtist : "Unknown Artist");
                                                    _music.setMusicLength(durationInMillis);
                                                    _music.setAlbumArtBitmap(albumArt);
                                                    _music.setUriFilePath(downloadUrl.toString());
                                                    _music.setResourceId(musicID);
                                                    _music.setFilePath(sortedItem.getName());
                                                    musicList.add(_music);

                                                    Log.d("StorageData", "Size: " + musicList.size());

                                                    musicAdapter.SetData(musicList);

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
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("StorageData", "Error: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e("error while upload music", e.getMessage());
        }
    }
}

