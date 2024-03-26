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
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dean.Music.music;
import com.example.dean.Utils.Utils;
import com.example.dean.account.AccountActivity;
import com.example.dean.account.AccountInfoActivity;
import com.example.dean.account.ChangePasswordActivity;
import com.example.dean.account.SignInActivity;
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
    TextView _tvUsername;
    String username;


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
        _tvUsername = view.findViewById(R.id._tvUsername);
        Switch darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        ConstraintLayout roundedCornersLayout = view.findViewById(R.id.roundedCorners);

        int currentMode = AppCompatDelegate.getDefaultNightMode();

        darkModeSwitch.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        roundedCornersLayout.setBackground(getResources().getDrawable(currentMode == AppCompatDelegate.MODE_NIGHT_YES ?
                R.drawable.rounded_corners_dark : R.drawable.rounded_corners_light));
        darkModeSwitch.setTextColor(getResources().getColor(currentMode == AppCompatDelegate.MODE_NIGHT_YES ?
                R.color.switch_text_color_dark : R.color.switch_text_color_light));

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                // Disable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
        username = Utils.getUserName();

        // Hiển thị username hoặc "Guest" tùy thuộc vào trạng thái đăng nhập
        if (Utils.isLoggedIn()) {
            _tvUsername.setText(username);
        } else {
            _tvUsername.setText("Guest");
        }

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
        switch (id) {
            case R.id.person_vector:
                if (!Utils.isLoggedIn()) {
                    // Inflate guest menu
                    PopupMenu guestMenu = new PopupMenu(requireContext(), getActivity().findViewById(R.id.person_vector));
                    guestMenu.getMenuInflater().inflate(R.menu.guest, guestMenu.getMenu());
                    guestMenu.setOnMenuItemClickListener(menuItem -> {
                        // Handle guest menu item clicks
                        switch (menuItem.getItemId()) {
                            case R.id.signIn:
                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, new SignInActivity())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.logIn:
                                fragmentManager = requireActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, new AccountActivity())
                                        .addToBackStack(null)
                                        .commit();
                                break;

                        }
                        return true;
                    });
                    guestMenu.show();
                } else {
                    // Inflate user menu
                    PopupMenu userMenu = new PopupMenu(requireContext(), getActivity().findViewById(R.id.person_vector));
                    userMenu.getMenuInflater().inflate(R.menu.user, userMenu.getMenu());
                    userMenu.setOnMenuItemClickListener(menuItem -> {
                        // Handle user menu item clicks
                        switch (menuItem.getItemId()) {
                            case R.id.account:
                                AccountInfoActivity accountInfoActivity = new AccountInfoActivity();
                                Bundle bundle = new Bundle();
                                bundle.putString("username", username);
                                accountInfoActivity.setArguments(bundle);
                                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, new AccountInfoActivity())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.changePass:
                                fragmentManager = requireActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, new ChangePasswordActivity())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.logOut:
                                Utils.setUsername(null);
                                Utils.setLoggedIn(false);
                                fragmentManager = requireActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, new MainMenuActivity())
                                        .addToBackStack(null)
                                        .commit();
                                break;

                        }
                        return true;
                    });
                    userMenu.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

