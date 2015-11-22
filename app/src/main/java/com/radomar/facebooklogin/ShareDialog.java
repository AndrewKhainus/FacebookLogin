package com.radomar.facebooklogin;

/**
 * Created by Radomar on 21.11.2015.
 */
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Radomar on 21.11.15.
 */
public class ShareDialog extends DialogFragment implements View.OnClickListener {

    private ImageView mImageViewSharePicture;
    private EditText mEditTextShareText;
    private Button mButtonAddShare;
    private Button mButtonCancel;

    private static final int Pick_image = 1;
    private Bitmap mBitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_share, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mImageViewSharePicture = (ImageView) view.findViewById(R.id.imgSharePicture_DFS);
        mEditTextShareText = (EditText) view.findViewById(R.id.etShareTxt_DFS);

        mButtonAddShare = (Button) view.findViewById(R.id.btnAdd_DFS);
        mButtonCancel = (Button) view.findViewById(R.id.btnCancel_DFS);


        mButtonAddShare.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);

        mImageViewSharePicture.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.imgSharePicture_DFS:
                addImage();
                break;
            case R.id.btnAdd_DFS:
                shareData();
                dismiss();
                break;
            case R.id.btnCancel_DFS:
                dismiss();
                break;
        }

    }

    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Pick_image);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Pick_image && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            final Uri imageUri = data.getData();
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImageViewSharePicture.setImageURI(imageUri);
        }
    }

    private void shareData(){

        String permissions = "me/feed";
        Bundle params = new Bundle();
        byte[] byteArray;

        if(mBitmap != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();
            params.putByteArray("source", byteArray);
            permissions = "me/photos";
        }

        params.putString("message", mEditTextShareText.getText().toString());

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                permissions,
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                    }
                }
        ).executeAsync();
    }
}
