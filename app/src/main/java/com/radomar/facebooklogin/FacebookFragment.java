package com.radomar.facebooklogin;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

/**
 * Created by Radomar on 14.11.2015
 */
public class FacebookFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = "sometag";

    LoginButton loginButton;
    private CallbackManager mCallbackManager;
    private String mUrlPictureUser;
    private ImageView mImageViewPictureUser;
    private TextView mUserInfo;

    private ShareDialog mShareDialog;
    private Button mShare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());
        mCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_facebook, container, false);

        loginButton = (LoginButton) view.findViewById(R.id.loginButton_FF);
        mImageViewPictureUser = (ImageView) view.findViewById(R.id.ivUserPhoto_FF);
        mUserInfo = (TextView) view.findViewById(R.id.tvUserInfo_FF);
        mShare = (Button) view.findViewById(R.id.btShare_FF);
        mShare.setOnClickListener(this);

        loginButton.setPublishPermissions("publish_actions");
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d(TAG, "onSuccess");
                getUserInformation();
            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(TAG, "onError");
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");

        if(AccessToken.getCurrentAccessToken() != null) {
            getUserInformation();
            Log.d(TAG, "getCurrentAccessToken --- " + AccessToken.getCurrentAccessToken().getToken());
//            mButtonLog.setText("Log Out");
//            mButtonShare.setVisibility(View.VISIBLE);
            Log.d(TAG, "some methods");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserInformation() {

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture, id, name");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try {
                            mUrlPictureUser = response.getJSONObject().
                                    getJSONObject("picture").
                                    getJSONObject("data").
                                    getString("url");

                            Log.d(TAG, response.getJSONObject().toString());
                            String id    = "Id: " + response.getJSONObject().getString("id");
                            String name  = "Name: " + response.getJSONObject().getString("name");

                            setData(mUrlPictureUser, id, name);

                            Log.d(TAG, "-" + response.toString());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();
    }

    private void setData(String url, String id, String name) throws JSONException {
        Picasso.with(getContext())
                .load(url)
                .into(mImageViewPictureUser);

        mUserInfo.setText(id + "\n" + name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btShare_FF:
                mShareDialog = new ShareDialog();
                mShareDialog.setCancelable(false);
                mShareDialog.show(getActivity().getFragmentManager(),"shareDialog");
                break;
        }
    }
}
