package com.radomar.facebooklogin.model;

import android.graphics.Bitmap;

/**
 * Created by Radomar on 10.01.2016
 */
public class ImageModel {

    public Bitmap imageBitmap;
    public byte[] bytes;

    public ImageModel(Bitmap imageBitmap, byte[] bytes) {
        this.imageBitmap = imageBitmap;
        this.bytes = bytes;
    }
}
