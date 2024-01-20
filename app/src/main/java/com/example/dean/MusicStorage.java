package com.example.dean;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MusicStorage {
    private static final String MUSIC_PREFERENCE = "music_preference";
    private static final String MUSIC_LIST_KEY = "music_list";

    public static List<music> getMusicList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MUSIC_PREFERENCE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(MUSIC_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<music>>() {}.getType();
        return gson.fromJson(json, type);
    }
}