package com.example.todoapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utility {
    public static Bitmap getPhoto(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
