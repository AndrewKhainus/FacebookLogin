package com.radomar.facebooklogin.task;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.radomar.facebooklogin.global.Constants;
import com.radomar.facebooklogin.model.ImageModel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Radomar on 09.01.2016
 */
public class ImageLoader extends AsyncTaskLoader<ImageModel> {

    private Uri mUri;
    private final static String TAG = "sometag";

    public ImageLoader(Context context, Bundle args) {
        super(context);
        if (args != null) {
            mUri = args.getParcelable(Constants.LOADER_URI_KEY);
            Log.d(TAG, this + "constructor, new Uri");
        }
    }

    @Override
    public ImageModel loadInBackground() {
        String pathFromURI = getRealPathFromURI(mUri);

        return new ImageModel(decodeSampledBitmapFromImage(pathFromURI, 150, 150),
                              getByteArrayFromImage());
    }

    private byte[] getByteArrayFromImage() {
        InputStream inputStream = null;
        try {
            inputStream = getContext().getContentResolver().openInputStream(mUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 64;
        byte[] buffer = new byte[bufferSize];

        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteBuffer.toByteArray();
    }

    public Bitmap decodeSampledBitmapFromImage(String pathName, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContext().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, this + "onStartLoading");
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        Log.d(TAG, this + "onForceLoad");
    }

    @Override
    public void onCanceled(ImageModel data) {
        super.onCanceled(data);
        Log.d(TAG, this + "onCanceled");
    }
}
