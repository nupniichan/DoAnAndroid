package com.example.dean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class musicListPageActivity extends AppCompatActivity {

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

        ImageButton musicOptionMenu = findViewById(R.id.musicOptionMenu);
        musicOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicOptionsBottomSheet bottomDialog = new MusicOptionsBottomSheet();
                bottomDialog.show(getSupportFragmentManager(),"bottom_dialog");
            }
        });
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