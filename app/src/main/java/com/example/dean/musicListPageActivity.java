package com.example.dean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class musicListPageActivity extends AppCompatActivity {

    private RecyclerView musicRecylerView;
    private MusicAdapter musicAdapter;
    private static final String MUSIC_PREFERENCE = "music_preference";
    private static final String MUSIC_LIST_KEY = "music_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_page);

        // Khởi tạo RecyclerView và Adapter
        musicRecylerView = findViewById(R.id.musicListRecyclerView);
        musicAdapter = new MusicAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        musicRecylerView.setLayoutManager(linearLayoutManager);

        // Lấy danh sách nhạc từ SharedPreferences và cập nhật RecyclerView
        musicAdapter.SetData(getMusicListFromStorage());
        musicRecylerView.setAdapter(musicAdapter);

        // Khởi tạo toolBar của trang danh sách nhạc
        Toolbar toolbar = findViewById(R.id.musicPageListToolBar);
        setSupportActionBar(toolbar);

        // Button quay về lại trang chủ
        Button homeButton = findViewById(R.id.button1);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToHomePage();
            }
        });
    }
    private List<music> getMusicList() {
        List<music> list = new ArrayList<>();
        list.add(new music(R.drawable.gochiusa,"Gochiusa","Rabbit House",270));
        return list;
    }
    private static final int PICK_AUDIO_REQUEST = 1;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.musiclistpage_toolbar,menu);
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
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedAudioUri = data.getData();

                // Sử dụng MediaMetadataRetriever để lấy thông tin chi tiết về tệp âm thanh
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, selectedAudioUri);

                // Lấy thông tin
                String audioTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String audioArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String audioDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                // Chuyển đổi đơn vị thời lượng thành milliseconds
                long durationInMillis = Long.parseLong(audioDuration);

                // Thêm tệp âm thanh vào danh sách âm nhạc
                List<music> musicList = getMusicList();
                musicList.add(new music(R.drawable.gochiusa, audioTitle, audioArtist, durationInMillis));
                musicAdapter.SetData(musicList);
            }
        }
    }

    public void ReturnToHomePage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private List<music> getMusicListFromStorage() {
        List<music> list;
        String json = getSharedPreferences(MUSIC_PREFERENCE, Context.MODE_PRIVATE).getString(MUSIC_LIST_KEY, null);
        if (json == null) {
            list = new ArrayList<>();
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<music>>() {}.getType();
            list = gson.fromJson(json, type);
        }
        return list;
    }

}