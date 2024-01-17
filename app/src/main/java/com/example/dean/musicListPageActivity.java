package com.example.dean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class musicListPageActivity extends AppCompatActivity {

    private RecyclerView musicRecylerView;
    private MusicAdapter musicAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_page);
        // Button quay về lại trang chủ
        Button homeButton = findViewById(R.id.button1);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReturnToHomePage();
            }
        });
        //
        // Khởi tạo toolBar của trang danh sách nhạc
        Toolbar toolbar = findViewById(R.id.musicPageListToolBar);
        setSupportActionBar(toolbar);
        musicRecylerView = findViewById(R.id.musicListRecyclerView);
        musicAdapter = new MusicAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        musicRecylerView.setLayoutManager(linearLayoutManager);

        musicAdapter.SetData(getMusicList());
        musicRecylerView.setAdapter(musicAdapter);
    }

    private List<music> getMusicList() {
        List<music> list = new ArrayList<>();
        list.add(new music(R.drawable.gochiusa,"Gochiusa","Rabbit House",270));
        list.add(new music(R.drawable.gochiusa,"Gochiusa","Rabbit House",270));
        list.add(new music(R.drawable.gochiusa,"Gochiusa","Rabbit House",270));
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.musiclistpage_toolbar,menu);
        return true;
    }
    public void ReturnToHomePage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}