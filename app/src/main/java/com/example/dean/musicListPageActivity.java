package com.example.dean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        // Xoá dữ liệu (nếu bị lỗi dữ liệu thì hẵn xoá chứ ko thôi lỗi đó)
/*        SharedPreferences sharedPreferences = getSharedPreferences(MUSIC_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();*/
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

    // Khi add file vào xong sẽ xử lý sự kiện là khi lấy xong mình sẽ đạt được gì
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedAudioUri = data.getData();
                // Sử dụng MediaMetadataRetriever để lấy thông tin chi tiết về tệp âm thanh
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, selectedAudioUri);
                String filePath = getRealPathFromUri(selectedAudioUri);

                // Lưu trữ tệp âm thanh MP3 vào bộ nhớ trong ứng dụng
                String mp3FileName = "audio_" + System.currentTimeMillis() + ".mp3";
                String mp3FilePath = saveMp3File(selectedAudioUri, mp3FileName);
                Log.d("mp3FilePath",mp3FilePath);
                retriever.setDataSource(this, selectedAudioUri);
                Log.d("filepath", filePath);

                // Lấy thông tin
                String audioTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String audioArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String audioDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                // Chuyển đổi đơn vị thời lượng thành milliseconds (vì khi lấy vô nó ở dạng miliseconds)
                long durationInMillis = Long.parseLong(audioDuration);

                // Lấy dữ liệu album art
                byte[] albumArtBytes = retriever.getEmbeddedPicture();
                Bitmap albumArtBitmap;
                albumArtBitmap = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);
                String albumArtFilePath = saveAlbumArtToFile(albumArtBitmap, "album_art_" + System.currentTimeMillis() + ".png");
                // Thêm tệp âm thanh vào danh sách âm nhạc và lưu trữ vào SharedPreferences
                List<music> musicList = getMusicListFromStorage();
                musicList.add(new music(R.drawable.gochiusa, audioTitle, audioArtist, durationInMillis,albumArtFilePath, albumArtBitmap, mp3FilePath ));
                musicAdapter.SetData(musicList);
                musicAdapter.notifyDataSetChanged();

                // Lưu trữ danh sách nhạc đã cập nhật vào SharedPreferences
                Gson gson = new Gson();
                String updatedJson = gson.toJson(musicList);
                getSharedPreferences(MUSIC_PREFERENCE, Context.MODE_PRIVATE).edit().putString(MUSIC_LIST_KEY, updatedJson).apply();
            }
        }
    }
    private String getRealPathFromUri(Uri uri) {
        String filePath = null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        if (documentFile != null) {
            filePath = documentFile.getUri().getPath();
        }
        return filePath;
    }
    private String saveAlbumArtToFile(Bitmap albumArtBitmap, String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir("albumArtDir", Context.MODE_PRIVATE);
        File filePath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            albumArtBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePath.getAbsolutePath();
    }
    private String saveMp3File(Uri audioUri, String fileName) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // Mở InputStream từ Uri của tệp âm thanh được chọn
            inputStream = getContentResolver().openInputStream(audioUri);

            // Tạo đường dẫn cho tệp âm thanh trong bộ nhớ trong của ứng dụng
            File directory = getDir("audioDir", Context.MODE_PRIVATE);
            File filePath = new File(directory, fileName);

            // Mở OutputStream để ghi dữ liệu vào tệp âm thanh
            outputStream = new FileOutputStream(filePath);

            // Sao chép dữ liệu từ InputStream sang OutputStream
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return filePath.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                // Đóng InputStream và OutputStream
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        musicAdapter.SetData(getMusicListFromStorage());
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