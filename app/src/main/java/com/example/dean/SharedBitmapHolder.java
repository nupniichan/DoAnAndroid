package com.example.dean;

import android.graphics.Bitmap;

public class SharedBitmapHolder {
    private static SharedBitmapHolder instance;
    private Bitmap sharedBitmap;

    private SharedBitmapHolder() {
        // Private constructor to prevent instantiation
    }

    public static synchronized SharedBitmapHolder getInstance() {
        if (instance == null) {
            instance = new SharedBitmapHolder();
        }
        return instance;
    }

    public Bitmap getSharedBitmap() {
        return sharedBitmap;
    }

    public void setSharedBitmap(Bitmap bitmap) {
        sharedBitmap = bitmap;
    }
}