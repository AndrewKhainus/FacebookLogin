package com.radomar.facebooklogin.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.radomar.facebooklogin.R;
import com.radomar.facebooklogin.global.Constants;
import com.radomar.facebooklogin.interfaces.ActionListener;
import com.radomar.facebooklogin.interfaces.GetCallbackInterface;
import com.radomar.facebooklogin.interfaces.OnStartAddAndRemoveListener;
import com.radomar.facebooklogin.model.ImageModel;
import com.radomar.facebooklogin.task.ImageLoader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Radomar on 05.01.2016
 */
public class FacebookFragment extends Fragment implements ActionListener,
                                                          View.OnClickListener,
                                                          LoaderManager.LoaderCallbacks<ImageModel> {


    private OnStartAddAndRemoveListener mOnStartAddAndRemoveListener;
    private GetCallbackInterface mGetCallbackInterface;

    private LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private ImageView mImageViewPictureUser;
    private ImageView mIvSelectedImage;
    private EditText mShareText;
    private TextView mUserInfo;
    private Button mShare;
    private Button mBtSelectImage;
    private ProgressBar mProgressBar;

    private Uri mImageUri;
    private byte[] mByteArray;

    private Bundle mLoaderBundle;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("sometag", "onAttach");
        mOnStartAddAndRemoveListener = (OnStartAddAndRemoveListener) activity;
        mGetCallbackInterface = (GetCallbackInterface) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("sometag", "onCreate");
        FacebookSdk.sdkInitialize(getActivity());
        mCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("sometag","onCreateView");

        View view = inflater.inflate(R.layout.fragment_facebook, container, false);

        findViews(view);
        setListener();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d("sometag","onViewCreated");

        initLoginButton();

        if(AccessToken.getCurrentAccessToken() != null && isNetworkConnected()) {
            retrieveUserDataRequest();
        }

        initImageLoader();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d("sometag", "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(Constants.URI_KEY);
        }
    }

        @Override
        public void onStart() {
        Log.d("sometag","onStart");
        super.onStart();
        mOnStartAddAndRemoveListener.onStartAddListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("sometag","onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.URI_KEY, mImageUri);
    }

    @Override
    public void onStop() {
        Log.d("sometag", "onStop");
        super.onStop();
        mOnStartAddAndRemoveListener.onStartRemoveListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnStartAddAndRemoveListener = null;
        mGetCallbackInterface = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();

            restartImageLoader();

        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
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

    private void initLoginButton() {
        mLoginButton.setPublishPermissions("publish_actions");
        // If using in a fragment
        mLoginButton.setFragment(this);
        // Other app specific specialization
        mLoginButton.registerCallback(mCallbackManager, mGetCallbackInterface.getFacebookCallback());
    }

    private void shareData(){
        if (isNetworkConnected()) {
            String permissions = "me/feed";
            Bundle params = new Bundle();

            if (mImageUri != null) {
                params.putByteArray("source", mByteArray);
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
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
        }
    }

    private void findViews(View view) {
        mLoginButton          =  (LoginButton)  view.findViewById(R.id.loginButton_FF);
        mImageViewPictureUser =  (ImageView)    view.findViewById(R.id.ivUserPhoto_FF);
        mUserInfo             =  (TextView)     view.findViewById(R.id.tvUserInfo_FF);
        mBtSelectImage        =  (Button)       view.findViewById(R.id.btSelectPhoto_FF);
        mIvSelectedImage      =  (ImageView)    view.findViewById(R.id.ivSelectedImage_FF);
        mShareText            =  (EditText)     view.findViewById(R.id.etTextShare_FF);
        mShare                =  (Button)       view.findViewById(R.id.btShare_FF);
        mProgressBar          =  (ProgressBar)  view.findViewById(R.id.pbProgressBar_FF);
    }

    private void setListener() {
        mBtSelectImage.setOnClickListener(this);
        mShare.setOnClickListener(this);
    }

    private void addImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.PICK_IMAGE);
    }

    private void parseResponseAndSetUserData(GraphResponse response) {
        response.getRequest().getTag();
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

    private void retrieveUserDataRequest() {

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture, id, name");

        if (mGetCallbackInterface != null) {
            GraphRequest graphRequest = new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "me",
                    parameters,
                    HttpMethod.GET,
                    mGetCallbackInterface.getGraphRequestCallback());
            graphRequest.setTag(Constants.LOGIN_REQUEST_TAG);
            graphRequest.executeAsync();
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
        if (response.getRequest().getTag() == Constants.LOGIN_REQUEST_TAG) {
            parseResponseAndSetUserData(response);
        } else {
            notifyUserAboutShareResult(response);
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void doAction(LoginResult loginResult) {
        retrieveUserDataRequest();
    }

    @Override
    public Loader<ImageModel> onCreateLoader(int id, Bundle args) {
        Loader<ImageModel> mLoader = null;

        if (id == Constants.BYTE_ARRAY_LOADER_ID) {
            mLoader = new ImageLoader(getActivity(), args);
            Log.d(Constants.TAG, "onCreateLoader" + mLoader);
        }
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<ImageModel> loader, ImageModel data) {
        Log.d(Constants.TAG, loader + "--- onLoadFinished ---" + data);
        mIvSelectedImage.setImageBitmap(data.imageBitmap);
        mByteArray = data.bytes;
    }

    @Override
    public void onLoaderReset(Loader<ImageModel> loader) {
    }

    private void initImageLoader() {
        initBundleForLoader();
        getLoaderManager().initLoader(Constants.BYTE_ARRAY_LOADER_ID, mLoaderBundle, this);
    }

    private void restartImageLoader() {
        initBundleForLoader();
        getLoaderManager().restartLoader(Constants.BYTE_ARRAY_LOADER_ID, mLoaderBundle, this);
    }

    private void initBundleForLoader() {
        mLoaderBundle = new Bundle();
        mLoaderBundle.putParcelable(Constants.LOADER_URI_KEY, mImageUri);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void notifyUserAboutShareResult(GraphResponse response) {
        try {
            if (response.getConnection().getResponseCode() == 200) {
                Toast.makeText(getActivity(), getString(R.string.good_news_everyone), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.very_bad), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

