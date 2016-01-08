package com.radomar.facebooklogin.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.radomar.facebooklogin.R;
import com.radomar.facebooklogin.interfaces.ActionListener;
import com.radomar.facebooklogin.interfaces.GetCallbackInterface;
import com.radomar.facebooklogin.interfaces.StartAddAndRemoveListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

/**
 * Created by Radomar on 05.01.2016
 */
public class FacebookFragment extends Fragment implements ActionListener,
                                                          View.OnClickListener {

    private static final int Pick_image = 1;
    public static final String IMAGE_URI = "imageUri";

    private StartAddAndRemoveListener mStartAddAndRemoveListener;
    private GetCallbackInterface mGetCallbackInterface;

    private LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private ImageView mImageViewPictureUser;
    private ImageView mSelectedImage;
    private EditText mShareText;
    private TextView mUserInfo;
    private Button mShare;
    private Button mBtSelectImage;

    private Uri mImageUri;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());
        mCallbackManager = CallbackManager.Factory.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mStartAddAndRemoveListener = (StartAddAndRemoveListener) activity;
        mGetCallbackInterface = (GetCallbackInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStartAddAndRemoveListener = null;
        mGetCallbackInterface = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mStartAddAndRemoveListener.StartAddListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mStartAddAndRemoveListener.StartRemoveListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_facebook, container, false);

        findViews(view);
        setListener();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mLoginButton.setPublishPermissions("publish_actions");
        // If using in a fragment
        mLoginButton.setFragment(this);
        // Other app specific specialization
        mLoginButton.registerCallback(mCallbackManager, mGetCallbackInterface.getFacebookCallback());

        if(AccessToken.getCurrentAccessToken() != null) {
            getUserInfoRequest();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btShare_FF:
                shareData();
                break;
            case R.id.btSelectPhoto_FF:
                addImage();
                break;
        }
    }

    private void shareData(){
        String permissions = "me/feed";
        Bundle params = new Bundle();

        if(mImageUri != null){
            params.putByteArray("source", readBytes(mImageUri));
            permissions = "me/photos";
        }

        params.putString("message", mShareText.getText().toString());

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                permissions,
                params,
                HttpMethod.POST,
                mGetCallbackInterface.getGraphRequestCallback()
        ).executeAsync();
    }

    public byte[] readBytes(Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = getActivity().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 4096;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Pick_image && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mSelectedImage.setImageURI(mImageUri);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void findViews(View view) {
        mLoginButton = (LoginButton) view.findViewById(R.id.loginButton_FF);
        mImageViewPictureUser = (ImageView) view.findViewById(R.id.ivUserPhoto_FF);
        mUserInfo = (TextView) view.findViewById(R.id.tvUserInfo_FF);

        mBtSelectImage = (Button) view.findViewById(R.id.btSelectPhoto_FF);
        mSelectedImage = (ImageView) view.findViewById(R.id.ivSelectedImage_FF);
        mShareText = (EditText) view.findViewById(R.id.etTextShare_FF);
        mShare = (Button) view.findViewById(R.id.btShare_FF);
    }

    private void setListener() {
        mBtSelectImage.setOnClickListener(this);
        mShare.setOnClickListener(this);
    }

    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Pick_image);
    }

    private void getUserInfo(GraphResponse response) {
        try {
            String mUrlPictureUser = response.getJSONObject().
                    getJSONObject("picture").
                    getJSONObject("data").
                    getString("url");

            String id    = "Id: " + response.getJSONObject().getString("id");
            String name  = "Name: " + response.getJSONObject().getString("name");

            setData(mUrlPictureUser, id, name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getUserInfoRequest() {

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture, id, name");

        if (mGetCallbackInterface != null) {
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "me",
                    parameters,
                    HttpMethod.GET,
                    mGetCallbackInterface.getGraphRequestCallback()
            ).executeAsync();
        }
    }

    private void setData(String url, String id, String name) throws JSONException {
        Picasso.with(getActivity())
                .load(url)
                .into(mImageViewPictureUser);

        mUserInfo.setText(String.format("%s\n%s", id, name));
    }

    @Override
    public void doAction(GraphResponse response) {
        Log.d("sometag", response.toString());
        getUserInfo(response);

    }

    @Override
    public void doAction(LoginResult loginResult) {
        getUserInfoRequest();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI, mImageUri);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(IMAGE_URI);
            mSelectedImage.setImageURI(mImageUri);
        }

    }
}

